package me.lightspeed7.mongofs

import java.io.File
import java.io.FileOutputStream
import scala.io.Source

// Test data for testing chunking
object LoremIpsum {

  val TEXT_FILE = "/resources/loremIpsum.txt"

  lazy val bytes: Array[Byte] = string.getBytes
  lazy val string: String = Source.fromFile(file).mkString

  lazy val file: File = {
    new File(baseDir(), TEXT_FILE)
  }

  @annotation.tailrec
  def baseDir(testDir: File = new File(".").getCanonicalFile): File = {
    println(s"BaseDir = " + testDir.getAbsolutePath())
    new File(testDir, TEXT_FILE).exists() match {
      case true  => testDir
      case false => baseDir(testDir.getParentFile)
    }
  }
}