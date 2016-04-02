import sbt._
import sbt.Keys._
import java.io.PrintWriter
import java.io.File
import sbtbuildinfo._
import sbtbuildinfo.BuildInfoKeys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.docker._

object ApplicationBuild extends Build {

  import Dependencies._

  val branch = "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = "git rev-parse --short HEAD".!!.trim

  organization := "me.lightspeed7"
  version := "0.10.0"

  println()
  println(s"App Version   => ${version}")
  println(s"Git Branch    => ${branch}")
  println(s"Git Commit    => ${commit}")
  println()

  // /////////////////////////////////////////////
  // Common Settings
  // /////////////////////////////////////////////
  lazy val commonSettings = Seq(
    scalaVersion := "2.11.8",

    //
    // Compile time optimizations
    publishArtifact in (Compile, packageDoc) := false, // Disable ScalDoc generation
    publishArtifact in packageDoc := false,
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++= Seq(ScalaTest, Slf4jApi),

    ivyXML := Dependencies.globalExclusions, // 

    //
    parallelExecution in Test := false, // Need to go sequentially within each test for now 
    javacOptions ++= Seq("-encoding", "UTF-8"), // force java to treat files as UTF-8 
    //
    // Scalaiform settings
    ScalariformKeys.preferences := ScalariformKeys.preferences.value // Scala formatting rules
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 60)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(CompactControlReadability, true)
      .setPreference(SpacesAroundMultiImports, true) //
      ) //  

  // /////////////////////////////////////////////
  // Libraries 
  // /////////////////////////////////////////////
  lazy val sync = Project("sync", file("sync"))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Seq(Gson, Guava, JUnit, JUnitInterface, Mockito, MongoJavaDriver))
    .settings(
      // BuildInfo
      buildInfoPackage := "me.lightspeed7.mongofs",
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion) :+ BuildInfoKey.action("buildTime") {
        System.currentTimeMillis
      })

  lazy val asyncReactive = Project("async-reactive", file("async-reactive"))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Seq(ReactiveMongo))
    .settings(
      // BuildInfo
      buildInfoPackage := "me.lightspeed7.mongofs",
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion) :+ BuildInfoKey.action("buildTime") {
        System.currentTimeMillis
      })

  lazy val root = Project("mongoFS", file(".")).aggregate(sync, asyncReactive)
}
