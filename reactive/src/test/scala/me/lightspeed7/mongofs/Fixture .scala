package me.lightspeed7.mongofs

import reactivemongo.api._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import reactivemongo.api.commands.GetLastError
import scala.concurrent.Future

object Fixture {

  implicit val ec = ExecutionContext.Implicits.global

  val driver = MongoDriver()
  val conOpts = MongoConnectionOptions(authMode = reactivemongo.api.ScramSha1Authentication)
  val username = "rootAdmin"
  val password = "password"
  val authDatabase = "admin"
  val timeout: FiniteDuration = 10 seconds
  val host = "services.local:27017,services.local:27018,services.local:27019"
  val databaseName = "MongoFSTest-fileStore"

  lazy val connection: MongoConnection = {
    val conn = driver.connection(host.split(","), options = conOpts)
    Await.result(conn.authenticate(authDatabase, username, password), timeout)
    conn
  }

  lazy val database: DefaultDB = result(connection.database(databaseName))

  def getConfig(bucket: String) = FileStoreConfig(database, bucket, GetLastError.Default, ReadPreference.primaryPreferred, timeout)
  def result[T](in: Future[T]): T = Await.result(in, timeout)
}