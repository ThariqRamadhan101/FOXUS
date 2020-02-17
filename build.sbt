name := "alpr-scala"

version := "0.1"

scalaVersion := "2.13.1"

val akkaVersion = "2.6.3"
val akkaHttpVersion = "10.1.11"
val scalaTestVersion = "3.1.0"
val specs2Version = "4.6.0"
val slickVersion = "3.3.2"
val postgresqlJdbcVersion = "42.2.9"
val jwtVersion = "4.2.0"

val javacppVersion = "1.5.2"
val opencvVersion = "4.1.2"

// Determine current platform
val platform = {
  // Determine platform name using code similar to javacpp
  // com.googlecode.javacpp.Loader.java line 60-84
  val jvmName = System.getProperty("java.vm.name").toLowerCase
  var osName = System.getProperty("os.name").toLowerCase
  var osArch = System.getProperty("os.arch").toLowerCase
  if (jvmName.startsWith("dalvik") && osName.startsWith("linux")) {
    osName = "android"
  } else if (jvmName.startsWith("robovm") && osName.startsWith("darwin")) {
    osName = "ios"
    osArch = "arm"
  } else if (osName.startsWith("mac os x")) {
    osName = "macosx"
  } else {
    val spaceIndex = osName.indexOf(' ')
    if (spaceIndex > 0) {
      osName = osName.substring(0, spaceIndex)
    }
  }
  if (osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586") || osArch
    .equals("i686")) {
    osArch = "x86"
  } else if (osArch.equals("amd64") || osArch.equals("x86-64") || osArch.equals(
    "x64"
  )) {
    osArch = "x86_64"
  } else if (osArch.startsWith("arm")) {
    osArch = "arm"
  }
  val platformName = osName + "-" + osArch
  println("platform: " + platformName)
  platformName
}

// Libraries with native dependencies
val bytedecoPresetLibs = Seq(
  "opencv" -> s"$opencvVersion-$javacppVersion",
  "ffmpeg" -> s"4.2.1-$javacppVersion",
  "openblas" -> s"0.3.7-$javacppVersion"
).flatMap {
  case (lib, ver) =>
    Seq(
      // Add both: dependency and its native binaries for the current platform
      "org.bytedeco" % lib % ver withSources () withJavadoc (),
      "org.bytedeco" % lib % ver classifier platform
    )
}

libraryDependencies ++= Seq(
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,

  // testing
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.specs2" %% "specs2-core" % specs2Version % "test",

  // slick FRM database
  // refer https://scala-slick.org/doc/3.3.1/gettingstarted.html
  "com.typesafe.slick" %% "slick" % slickVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "org.postgresql" % "postgresql" % postgresqlJdbcVersion, //org.postgresql.ds.PGSimpleDataSource dependency

  // JWT
  "com.pauldijou" %% "jwt-spray-json" % jwtVersion,

  // javacpp
  "org.bytedeco" % "javacv" % javacppVersion withSources () withJavadoc (),

  // tesseract
  "net.sourceforge.tess4j" % "tess4j" % "4.0.0"
)

libraryDependencies ++= bytedecoPresetLibs
