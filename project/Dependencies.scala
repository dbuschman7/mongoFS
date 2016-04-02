import sbt._
import sbt.Keys._

object Dependencies {

  implicit def dependencyFilterer(dep: ModuleID) = new Object {
    def excluding(group: String, artifactId: String) =
      dep.exclude(group, artifactId)
  }

  implicit def dependencyFiltererSeq(deps: Seq[ModuleID]) = new Object {
    def excluding(group: String, artifactId: String) =
      deps.map(_.exclude(group, artifactId))
  }

  // Versions
  val Slf4jVersion = "1.7.9"

  // Scala
  val ScalaLib = "org.scala-lang" % "scala-library" % "2.11.8"
  val ScalaReflect = "org.scala-lang" % "scala-library" % "2.11.8"

  // Test 
  val GuavaTestLib = "com.google.guava" % "guava-testlib" % "17.0" % "test"
  val Gson = "com.google.code.gson" % "gson" % "2.2.4" % "test"
  val JUnit = "junit" % "junit" % "4.12" % "test"
  val JUnitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"
  val Mockito = "org.mockito" % "mockito-all" % "1.10.17" % "test"
  val ScalaTest = "org.scalatest" %% "scalatest" % "2.2.5" % "test" excluding ("org.scala-lang", "scala-library") excluding ("org.scala-lang", "scala-reflect")

  
  
  // Third Party
  val Enumeratum = "com.beachape" %% "enumeratum-play-json" % "1.3.1" excluding ("org.scala-lang", "scala-library")
  val FindBugs = "com.google.code.findbugs" % "jsr305" % "1.3.9"
  val Guava = "com.google.guava" % "guava" % "17.0"
  val Java8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0"

  val MongoJavaDriver = "org.mongodb" % "mongo-java-driver" % "3.2.2"

  val PlayJson = "com.typesafe.play" %% "play-json" % "2.4.2" excluding ("org.scala-lang", "scala-library")
  val ReactiveMongo = "org.reactivemongo" %% "reactivemongo" % "0.11.5" excluding ("org.scala-lang", "scala-library") excluding ("org.apache.logging.log4j", "log4j-core")

  val Slf4jApi = "org.slf4j" % "slf4j-api" % Slf4jVersion
  val Slf4jSimple = "org.slf4j" % "slf4j-simple" % Slf4jVersion % "test"

  val globalExclusions =
    <dependencies>
      <exclude org="javax.jms" module="jms"/>
      <exclude org="com.sun.jdmk" module="jmxtools"/>
      <exclude org="com.sun.jmx" module="jmxri"/>
      <exclude module="slf4j-jdk14"/>
      <exclude module="slf4j-log4j"/>
      <exclude module="slf4j-log4j12"/>
      <exclude module="slf4j-simple"/>
      <exclude module="cglib-nodep"/>
    </dependencies>
}
