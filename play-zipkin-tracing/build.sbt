lazy val commonSettings = Seq(
  organization := "jp.co.bizreach",
  version := "2.0.1-talend",
  scalaVersion := "2.11.8",
//  crossScalaVersions := Seq("2.11.8", "2.12.2"),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := (
    <url>https://github.com/bizreach/play-zipkin-tracing</url>
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/bizreach/play-zipkin-tracing</url>
      <connection>scm:git:https://github.com/bizreach/play-zipkin-tracing.git</connection>
    </scm>
    <developers>
      <developer>
        <id>nishiyama</id>
        <name>Hajime Nishiyama</name>
      </developer>
      <developer>
        <id>shimamoto</id>
        <name>Takako Shimamoto</name>
      </developer>
      <developer>
        <id>takezoe</id>
        <name>Naoki Takezoe</name>
      </developer>
    </developers>
  )
)

val play26Version = "2.6.6"
//val play25Version = "2.5.7"
//val play24Version = "2.4.8"
//val play23Version = "2.3.10"

val akkaVersion = "2.5.4-talend"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "play-zipkin-tracing",
    publishArtifact := false
  ).
  aggregate(core, akka, play26)

lazy val core = (project in file("core")).
  settings(commonSettings: _*).
  settings(addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)).
  settings(
    name := "play-zipkin-tracing-core",
    libraryDependencies ++= Seq(
      "commons-lang" % "commons-lang" % "2.6",
      "io.zipkin.brave" % "brave" % "4.12.0",
      "io.zipkin.reporter2" % "zipkin-sender-okhttp3" % "2.3.0",
      "io.zipkin.reporter2" % "zipkin-sender-kafka11" % "2.3.0",
      "org.scala-lang" % "scala-reflect" % "2.11.8",
      "org.scalatest" %% "scalatest" % "3.0.3" % "test",
      "io.zipkin.brave" % "brave-tests" % "4.12.0" % "test",
      "junit" % "junit" % "4.12" % "test",
      "com.novocode" % "junit-interface" % "0.11" % "test"
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
  )

lazy val akka = (project in file("akka")).
  settings(commonSettings: _*).
  settings(
    name := "play-zipkin-tracing-akka",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % play26Version % Provided,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided
    )
  ).dependsOn(
    core % "test->test;compile->compile"
  )

lazy val play26 = (project in file("play26")).
  settings(commonSettings: _*).
  settings(
    name := "play-zipkin-tracing-play26",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % play26Version % Provided,
      "com.typesafe.play" %% "play-ws" % play26Version % Provided,
      "com.typesafe.play" %% "play-guice" % play26Version % Test
    )
  ).dependsOn(
    core % "test->test;compile->compile",
    akka % "test->test;compile->compile"
  )

//lazy val play25 = (project in file("play25")).
//  settings(commonSettings: _*).
//  settings(
//    name := "play-zipkin-tracing-play25",
//    libraryDependencies ++= Seq(
//      "com.typesafe.play" %% "play" % play25Version % Provided,
//      "com.typesafe.play" %% "play-ws" % play25Version % Provided
//    )
//  ).dependsOn(core % "test->test;compile->compile")
//
//lazy val play24 = (project in file("play24")).
//  settings(commonSettings: _*).
//  settings(
//    name := "play-zipkin-tracing-play24",
//    libraryDependencies ++= Seq(
//      "com.typesafe.play" %% "play" % play24Version % Provided,
//      "com.typesafe.play" %% "play-ws" % play24Version % Provided
//    )
//  ).dependsOn(core % "test->test;compile->compile")
//
//
//lazy val play23 = (project in file("play23")).
//  settings(commonSettings: _*).
//  settings(
//    name := "play-zipkin-tracing-play23",
//    libraryDependencies ++= Seq(
//      "com.typesafe.play" %% "play" % play23Version % Provided,
//      "com.typesafe.play" %% "play-ws" % play23Version % Provided
//    ),
//    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
//  ).dependsOn(core % "test->test;compile->compile")
