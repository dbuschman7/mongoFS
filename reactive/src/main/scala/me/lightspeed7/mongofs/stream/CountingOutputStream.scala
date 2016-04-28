package me.lightspeed7.mongofs.stream

import java.io.OutputStream
import java.io.IOException
import me.lightspeed7.mongofs._
import me.lightspeed7.mongofs.url.MongoFileUrl
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

class CountingOutputStream(stats: MongoFileStatistics, out: OutputStream)(implicit val store: MongoFileStore) extends OutputStream {

  @throws(classOf[IOException])
  def write(b: Int): Unit = { stats.incrementTotal(1); out.write(b) }

  @throws(classOf[IOException])
  override def write(b: Array[Byte]): Unit = { stats.incrementTotal(b.length); out.write(b) }

  @throws(classOf[IOException])
  override def write(b: Array[Byte], o: Int, l: Int): Unit = { stats.incrementTotal(l - o); out.write(b, o, l) }

}