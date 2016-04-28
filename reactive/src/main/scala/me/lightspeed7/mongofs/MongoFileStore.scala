package me.lightspeed7.mongofs

import reactivemongo.bson.BSONDocument
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import sun.security.pkcs11.Secmod
import reactivemongo.bson.Macros
import java.util.zip.GZIPInputStream
import scala.util._
import org.slf4j.LoggerFactory
import me.lightspeed7.mongofs.url.MongoFileUrl
import me.lightspeed7.mongofs.url.StorageFormat
import java.io.InputStream
import play.api.libs.iteratee._
import org.joda.time.DateTime
import me.lightspeed7.mongofs.stream.MongoFileStatistics
import me.lightspeed7.mongofs.stream.FileChunksOutputStream
import java.io.OutputStream
import java.util.zip.GZIPOutputStream
import me.lightspeed7.mongofs.stream.MongoFileStatistics

case class MongoFileStore private (config: FileStoreConfig)(implicit ex: ExecutionContext) {

  private val log = LoggerFactory.getLogger(classOf[MongoFileStore])

  private def idSelector(id: ObjectId) = BSONDocument("_id" -> id)
  private def filesIdSelector(fileId: ObjectId) = BSONDocument("files_id" -> fileId)

  // 
  // Read Operations 
  // ///////////////////////
  def findOne(id: ObjectId): Future[Option[MongoFile]] = {
    config.filesCollection.find(idSelector(id)).one[MongoFile](config.readPreference)
  }

  private[mongofs] case class BinData(data: Array[Byte])
  private[mongofs] object BinData {
    implicit val _mongo = Macros.handler[BinData]
  }

  private def chunkData(chunk: MongoFileChunk): Future[Array[Byte]] = {
    import me.lightspeed7.mongofs._
    config.chunksCollection
      .find(idSelector(chunk._id))
      .projection(BSONDocument("data" -> 1, "_id" -> 0))
      .one[BinData](config.readPreference)
      .map { b =>
        b.map(_.data).getOrElse(Array())
      }
  }

  private[mongofs] def getChunks(file: MongoFile): Future[List[FileChunkReader]] = {
    println("Start of  getChunks")

    config.chunksCollection
      .find(filesIdSelector(file._id))
      .projection(BSONDocument("files_id" -> 1, "n" -> 1, "sz" -> 1))
      .sort(BSONDocument("n" -> 1))
      .cursor[MongoFileChunk](config.readPreference)
      .collect[List]()
      .map { c => println(s"Have chunks from database - ${c.size}"); c }
      .map { chunks => chunks.map { ch => FileChunkReader(ch)(chunkData) } }
  }

  def fileData(file: MongoFile): Stream[Byte] = {
    val in = new MergeChunksInputStream(file)(this)
    val url = file.url

    // first decryption 
    val decrypt = (config.crypto, url.isStoredEncrypted()) match {
      case (Some(crypto), true) => new DecryptInputStream(config.crypto.get, file.storage, in)
      case (None, true)         => throw new IllegalStateException("Found encrypted file with no crypto configured")
      case (_, false)           => in
    }

    // second, compression
    val hydrated = url.isStoredCompressed() match {
      case true  => new GZIPInputStream(decrypt)
      case false => decrypt
    }

    Stream.continually(hydrated.read).takeWhile(-1 !=).map { _.toByte }
  }

  //
  // Write Operations 
  // ////////////////////////////
  private[mongofs] def writeChunk(chunk: MongoFileChunk): Future[MongoFileChunk] = {
    Try(config.chunksCollection.insert[MongoFileChunk](chunk, config.writeConcern)) match {
      case Success(future) => future.map(wr => chunk)
      case Failure(ex)     => log.error("Unable to insert Chunk", ex); throw ex
    }
  }

  private[mongofs] def writeFile(file: MongoFile): Future[MongoFile] = {
    Try(config.filesCollection.insert(file, config.writeConcern)) match {
      case Success(future) => future.map(wr => file)
      case Failure(ex)     => log.error("Unable to insert File", ex); throw ex
    }
  }

  def newFileUrl(
    filename: String,
    contentType: String,
    compress: Boolean = config.compression,
    encrypted: Boolean = config.crypto.isDefined //
  ): MongoFileUrl = MongoFileUrl.construct(ObjectId.generate.bson, filename, contentType, StorageFormat.detect(compress, encrypted))

  def newFileFromStream(
    filename: String,
    contentType: String,
    compress: Boolean = config.compression,
    encrypted: Boolean = config.crypto.isDefined, //
    expiresAt: Option[DateTime] = None,
    stream: InputStream
  ): Future[MongoFile] = {

    val url = newFileUrl(filename, contentType, compress, encrypted)
    val enum = Enumerator.fromStream(stream, config.chunkSize.getChunkSize)

    newFile(url, expiresAt, enum)
  }

  def newFileFromEnumerator(
    filename: String,
    contentType: String,
    compress: Boolean = config.compression,
    encrypted: Boolean = config.crypto.isDefined, //
    expiresAt: Option[DateTime] = None,
    enum: Enumerator[Array[Byte]]
  ): Future[MongoFile] = {

    val url = newFileUrl(filename, contentType, compress, encrypted)

    newFile(url, expiresAt, enum)
  }

  def newFile(
    url: MongoFileUrl,
    expiresAt: Option[DateTime] = None,
    enum: Enumerator[Array[Byte]]
  ): Future[MongoFile] = {

    implicit val store = this
    val stats = new MongoFileStatistics(url, expiresAt)
    val sink = generateSink(url, stats, expiresAt)

    val iter: Iteratee[Array[Byte], Unit] = Iteratee.fold[Array[Byte], Unit]() { (total, buf) => sink.write(buf) }
    enum |>> iter flatMap { iter => stats.writeFile }
  }

  private[mongofs] def generateSink(
    url: MongoFileUrl,
    stats: MongoFileStatistics,
    expiresAt: Option[DateTime]
  )(implicit store: MongoFileStore): OutputStream = {

    val bottom: OutputStream = new BufferedChunksOutputStream(new FileChunksOutputStream(stats), config.chunkSize.getChunkSize)

    (url.isStoredCompressed(), config.crypto) match {
      case (true, Some(crypto))  => new EncryptChunkOutputStream(crypto, new GZIPOutputStream(bottom))
      case (false, Some(crypto)) => new EncryptChunkOutputStream(crypto, bottom)
      case (true, None)          => new GZIPOutputStream(bottom)
      case (_, _)                => bottom
    }
  }
}

object MongoFileStore {
  def create(config: FileStoreConfig)(implicit ex: ExecutionContext): MongoFileStore = MongoFileStore(config)
}