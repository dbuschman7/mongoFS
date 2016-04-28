package me.lightspeed7.mongofs

import java.io.ByteArrayOutputStream

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.scalatest.Finders
import org.scalatest.FunSuite
import org.scalatest.Matchers._

import me.lightspeed7.mongofs.crypto.BasicCrypto
import me.lightspeed7.mongofs.util.ChunkSize
import play.api.libs.iteratee.Enumerator
import org.joda.time.DateTime

class MongoFileStoreTest extends FunSuite {

  import Fixture._

  val config = getConfig("fileStore").withCrypto(new BasicCrypto(ChunkSize.tiny_4K))
  val store = MongoFileStore.create(config)

  /**
   *  {
   * "_id": ObjectId("57049542e822544474c7ab2d"),
   * "uploadDate": new Date(1459918146870),
   * "chunkSize": NumberLong(3384),
   * "filename": "loremIpsum.txt",
   * "contentType": "text/plain",
   * "format": "encgz",
   * "chunkCount": 3,
   * "length": NumberLong(32087),
   * "md5": "824ab5bef28d9af51d9b9d146e4356be",
   * "storage": NumberLong(9235),
   * "ratio": 0.28781126312836974313
   * }
   */

  test("Read file object and all chunks") {
    val id = ObjectId("57049542e822544474c7ab2d")

    // file 
    val file = result(store.findOne(id))
    file.isDefined should be(true)

    val mf = file.get
    mf._id.id should be("57049542e822544474c7ab2d")
    mf.chunkCount should be(3)
    mf.chunkSize should be(3384L)
    mf.contentType should be("text/plain")
    mf.filename should be("loremIpsum.txt")
    mf.format should be("encgz")
    mf.length should be(32087)
    mf.manifestId should be(None)
    mf.md5 should be("824ab5bef28d9af51d9b9d146e4356be")
    mf.ratio should be(0.28781126312836974313)
    mf.storage should be(9235)
    mf.uploadDate.getMillis should be(1459918146870L)

    // chunks
    println("Ready to read the file data")
    val out = new ByteArrayOutputStream();

    val stream = store.fileData(mf)
    println("have stream")
    Thread.sleep(5000)
    println("End of sleep hold")
    stream.foldLeft(out) { (out, in) => out.write(in); out }

    val output = out.toString()
    output.length should be(32087L)
    output should be(LoremIpsum.string)

  }

  test("Test Writing a file") {

    val enum = Enumerator.fromFile(LoremIpsum.file, ChunkSize.tiny_4K.getChunkSize)
    val file = Await.result(store.newFileFromEnumerator("loremIpsum.txt", "text/plain", true, true, None, enum), 5 seconds)
    println(s"Written file id = ${file._id}")
    file.chunkCount should be(3)
    file.chunkSize should be(3384L)
    file.contentType should be("text/plain")
    file.filename should be("loremIpsum.txt")
    file.format should be("encgz")
    file.length should be(32087L)
    file.manifestId should be(None)
    file.md5 should be("824ab5bef28d9af51d9b9d146e4356be")
    file.ratio should be(0.28781126312836974313)
    file.storage should be(9235)
    (DateTime.now.getMillis - file.uploadDate.getMillis) should be < (10L * 1000)
  }

}