package me.lightspeed7

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.UUID
import reactivemongo.api._
import reactivemongo.api.commands.GetLastError
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes._
import reactivemongo.bson._
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import me.lightspeed7.mongofs.url.MongoFileUrl
import me.lightspeed7.mongofs.url.StorageFormat
import me.lightspeed7.mongofs.crypto.Crypto
import java.io.InputStream
import me.lightspeed7.mongofs.util.ChunkSize

package object mongofs {

  //
  // Reactive Mongo Handlers
  // ///////////////////////////
  implicit object DateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(t: BSONDateTime) = new DateTime(t.value)
    def write(t: DateTime) = BSONDateTime(t.getMillis)
  }

  implicit object DateTimeZoneHandler extends BSONHandler[BSONString, DateTimeZone] {
    def read(t: BSONString) = DateTimeZone.forID(t.value)
    def write(t: DateTimeZone) = BSONString(t.getID)
  }

  implicit object UuidHandler extends BSONHandler[BSONString, UUID] {
    def read(t: BSONString) = UUID.fromString(t.value)
    def write(t: UUID) = BSONString(t.toString)
  }

  implicit object ObjectIdHandler extends BSONHandler[BSONObjectID, ObjectId] {
    def read(t: BSONObjectID) = ObjectId(t.stringify)
    def write(t: ObjectId) = BSONObjectID(t.id)
  }

  //
  // Domain Objects
  // ////////////////////////
  case class ObjectId(id: String) {
    val bson = new org.bson.types.ObjectId(id)
  }
  object ObjectId {
    def generate: ObjectId = ObjectId(new org.bson.types.ObjectId().toString())
  }

  case class MongoFile(
      _id: ObjectId,
      uploadDate: DateTime,
      chunkSize: Long,
      filename: String,
      contentType: String,
      format: String,
      chunkCount: Int,
      length: Long,
      md5: String,
      storage: Long,
      ratio: Double,
      manifestId: Option[String] //
  ) {
    val url: MongoFileUrl = MongoFileUrl.construct(_id.bson, filename, contentType, StorageFormat.find(format))
  }

  object MongoFile {
    implicit val _mongo = Macros.handler[MongoFile]
  }

  case class MongoFileChunk(
      _id: ObjectId,
      files_id: ObjectId,
      n: Int, // chunk number 
      sz: Int, // size of data
      expireAt: Option[DateTime] = None,
      data: Option[Array[Byte]] = None //
  ) {
    def withExpires(expires: DateTime): MongoFileChunk = copy(expireAt = Option(expires))
  }

  object MongoFileChunk {
    implicit val _mongo = Macros.handler[MongoFileChunk]
  }

  case class FileChunkReader(chunk: MongoFileChunk)(reader: MongoFileChunk => Future[Array[Byte]])(implicit ec: ExecutionContext) {
    def getDataStream: ByteArrayInputStream = {
      println(s"Fetching Chunk data - ${chunk.n}")
      val f = reader(chunk).map { bytes =>
        new ByteArrayInputStream(bytes)
      }
      Await.result(f, 10 seconds)
    }
  }

  //
  // Config 
  // ///////////////////////
  case class FileStoreConfig(
      db: DefaultDB,
      bucket: String,
      writeConcern: GetLastError,
      readPreference: ReadPreference,
      chunkSize: ChunkSize,
      queryTimeout: Duration, //
      compression: Boolean = true, //
      crypto: Option[Crypto] = None // 
  )(implicit ec: ExecutionContext) {

    def withCrypto(crypto: Crypto) = copy(crypto = Option(crypto))

    private[mongofs] lazy val filesCollection: BSONCollection = {
      FileStoreConfig.getCollection(this, s"${bucket}.files", //
        Index(Seq(("manifestId", IndexType.Ascending)), background = true),
        Index(Seq(("filename", IndexType.Ascending)), background = true),
        Index(Seq(("ttl", IndexType.Ascending)), background = true))
    }

    private[mongofs] lazy val chunksCollection: BSONCollection = {
      FileStoreConfig.getCollection(this, s"${bucket}.chunks", //
        Index(Seq(("ttl", IndexType.Ascending)), background = true),
        Index(Seq(("files_id", IndexType.Ascending)), background = true))
    }

  }

  object FileStoreConfig {
    private val log = LoggerFactory.getLogger(classOf[FileStoreConfig])

    private[mongofs] def getCollection(config: FileStoreConfig, name: String, indexes: Index*)(implicit ec: ExecutionContext): BSONCollection = {
      val exists = Await.result(config.db.collectionNames, config.queryTimeout).filter(_.contains(name)).headOption
      exists match {
        case None => {
          val coll = BSONCollection(config.db, name, FailoverStrategy(config.queryTimeout.asInstanceOf[FiniteDuration]))
          Await.result(coll.create(true), config.queryTimeout)
        }
        case Some(name) => log.info(s"Collection ${name} already exists")
      }

      val coll = config.db[BSONCollection](name)

      indexes.map { idx =>
        Await.result(coll.indexesManager.ensure(idx), config.queryTimeout)
      }

      coll
    }
  }

  class MergeChunksInputStream(file: MongoFile)(implicit store: MongoFileStore) extends InputStream {

    var chunks: List[FileChunkReader] = Await.result(store.getChunks(file), store.config.queryTimeout) // TODO : fix me 

    var current = rotate

    def rotate: Option[ByteArrayInputStream] = chunks.size match {
      case 0 => None
      case _ => {
        val next = chunks.headOption
        chunks = chunks.tail
        next.map(_.getDataStream)
      }
    }

    def read(): Int = current match {
      case None => -1
      case Some(buf) => {
        buf.available() match {
          case 0 => // rotate to the next chunk
            current = rotate
            this.read() // recurse 
          case _ => buf.read
        }
      }
    }

  }

}