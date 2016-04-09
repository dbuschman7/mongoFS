package me.lightspeed7.mongofs.stream

import java.io.OutputStream
import java.io.IOException
import me.lightspeed7.mongofs._
import me.lightspeed7.mongofs.url.MongoFileUrl
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

class FileChunksOutputStream(
    url: MongoFileUrl,

    expiresAt: Option[DateTime] = None)(implicit val store: MongoFileStore) extends OutputStream {

  @throws(classOf[IOException])
  def write(b: Int): Unit = { throw new IllegalStateException("Single byte writing not supported with this OutputStream") }

  @throws(classOf[IOException])
  override def write(b: Array[Byte]): Unit = {
    Option(b) match {
      case None      => throw new IllegalArgumentException("buffer cannot be null")
      case Some(buf) => super.write(buf, 0, buf.length);
    }
  }

  /**
   * The methods copied Largely from the Java impl, will optimze later
   */
  @throws(classOf[IOException])
  override def write(buffer: Array[Byte], offset: Int, length: Int): Unit = {

    var internal = buffer // assume the whole passed in buffer for efficiency

    // if partial buffer, then we have to copy the data until serialized
    if (offset != 0 || length != buffer.length) {
      internal = new Array[Byte](length)
      System.arraycopy(buffer, offset, internal, 0, length);
    }

  }

  def getFileId: ObjectId = ObjectId(url.getMongoFileId.toString())

  class ChunksStatisticsAdapter {
    import java.security.MessageDigest
    import java.security.NoSuchAlgorithmException

    val chunksCount = new AtomicInteger(0);
    val totalBytes = new AtomicLong(0);

    val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

    /**
     *  Returns chunk number
     */
    def writeChunk(buffer: Array[Byte]): Future[MongoFileChunk] = {
      messageDigest.update(buffer)
      totalBytes.addAndGet(buffer.length)
      val n = chunksCount.incrementAndGet()

      val chunk = MongoFileChunk(ObjectId.generate, ObjectId(url.getMongoFileId.toString()), n, buffer.length, Some(buffer))

      store.writeChunk(chunk)
    }

  }

}