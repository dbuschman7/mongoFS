package me.lightspeed7.mongofs

import reactivemongo.bson.BSONDocument
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import sun.security.pkcs11.Secmod
import reactivemongo.bson.Macros
import java.util.zip.GZIPInputStream
import scala.util._
import org.slf4j.LoggerFactory

case class MongoFileStore private (config: FileStoreConfig)(implicit ex: ExecutionContext) {

  private val log = LoggerFactory.getLogger(classOf[MongoFileStore])

  private def idSelector(id: ObjectId) = BSONDocument("_id" -> id)
  private def filesIdSelector(fileId: ObjectId) = BSONDocument("files_id" -> fileId)

  def findOne(id: ObjectId): Future[Option[MongoFile]] = {
    config.filesCollection.find(idSelector(id)).one[MongoFile](config.readPreference)
  }

  case class BinData(data: Array[Byte])
  object BinData {
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

  private[mongofs] def writeChunk(chunk: MongoFileChunk): Future[MongoFileChunk] = {
    Try(config.chunksCollection.insert[MongoFileChunk](chunk, config.writeConcern)) match {
      case Success(future) => future.map(wr => chunk)
      case Failure(ex)     => log.error("Unable to insert Entity", ex); throw ex
    }
  }

  private[mongofs] def fromInputStream(s: java.io.InputStream) = Stream.continually(s.read).takeWhile(-1 !=).map { _.toByte }

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

    fromInputStream(hydrated)
  }
}

object MongoFileStore {
  def create(config: FileStoreConfig)(implicit ex: ExecutionContext): MongoFileStore = MongoFileStore(config)
}