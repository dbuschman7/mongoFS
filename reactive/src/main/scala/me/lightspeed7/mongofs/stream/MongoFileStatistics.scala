package me.lightspeed7.mongofs.stream

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future
import me.lightspeed7.mongofs._
import me.lightspeed7.mongofs.url._
import org.joda.time.DateTime
import me.lightspeed7.mongofs.util.FileUtil
import me.lightspeed7.mongofs.MongoFileChunk
import scala.concurrent.ExecutionContext

class MongoFileStatistics(url: MongoFileUrl, expiresAt: Option[DateTime])(implicit store: MongoFileStore, ex: ExecutionContext) {
  import java.security.MessageDigest
  import java.security.NoSuchAlgorithmException

  val chunksCount = new AtomicInteger(0)
  val total = new AtomicLong(0)
  val storage = new AtomicLong(0)
  val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

  val chunks = new java.util.ArrayList[Future[MongoFileChunk]]()

  def incrementTotal(delta: Int) = total.addAndGet(delta)

  /**
   *  Returns chunk number
   */
  def writeChunk(buffer: Array[Byte]): Future[MongoFileChunk] = {
    messageDigest.update(buffer)
    storage.addAndGet(buffer.length)
    val currentChunk = chunksCount.incrementAndGet()

    val chunk = MongoFileChunk(
      _id = ObjectId.generate,
      files_id = ObjectId(url.getMongoFileId.toString()),
      n = currentChunk,
      sz = buffer.length,
      expireAt = expiresAt,
      data = Some(buffer) //
    )

    val future = store.writeChunk(chunk)
    chunks.add(future)
    future
  }

  def writeFile: Future[MongoFile] = {

    val _id: ObjectId = ObjectId(url.getMongoFileId.toString())
    val updateDate: DateTime = DateTime.now
    val chunkSize: Long = store.config.chunkSize.getChunkSize.toLong
    val length = total.get
    val filename: String = url.getFileName
    val contentType: String = url.getMediaType
    val format: String = url.getFormat.getCode
    val chunkCount: Int = chunksCount.get
    val md5: String = FileUtil.toHex(messageDigest.digest())
    val ratio: Double = if (storage == 0.0) 0.0 else storage.get.toDouble / length

    val file = MongoFile(_id, updateDate, chunkSize, filename, contentType, format, chunkCount, length, md5, storage.get, ratio, None)

    // make sure all the chunks have been committed saving the file 
    import scala.collection.JavaConverters._
    Future.sequence(chunks.asScala).flatMap { _ => store.writeFile(file) }
  }

}
