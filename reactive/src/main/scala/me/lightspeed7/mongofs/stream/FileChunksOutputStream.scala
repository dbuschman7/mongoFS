package me.lightspeed7.mongofs.stream

import java.io.OutputStream
import java.io.IOException
import me.lightspeed7.mongofs._
import me.lightspeed7.mongofs.url.MongoFileUrl
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

class FileChunksOutputStream(stats: MongoFileStatistics)(implicit val store: MongoFileStore) extends OutputStream {

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

    stats.writeChunk(internal)
  }

}