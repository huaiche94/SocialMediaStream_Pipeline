name := "DataDemo_A_Ingestion"

version := "0.1"

scalaVersion := "2.12.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Confluent" at "http://packages.confluent.io/maven/",
  "Jcenter" at "https://jcenter.bintray.com"
)

lazy val dependencies = Seq(
  "org.apache.avro" % "avro" % Versions.Avro,
  "org.apache.kafka" % "kafka-streams" % Versions.Kafka,
  "org.apache.kafka" %% "kafka-streams-scala" % Versions.Kafka,
  "io.confluent" % "kafka-avro-serializer" % Versions.Confluent,
  "com.typesafe.scala-logging" %% "scala-logging" % Versions.ScalaLogging,
  "org.slf4j" % "slf4j-api" % Versions.Slf4j,
  "org.slf4j" % "log4j-over-slf4j" % Versions.Slf4j,
  "ch.qos.logback" % "logback-classic" % Versions.Logback,
  "net.dean.jraw" % "JRAW" % Versions.JRAW,
  "com.danielasfregola" %% "twitter4s" % Versions.Twitter4s,
  "com.typesafe" % "config" % Versions.TypesafeConfig
)

dependencyOverrides ++= Seq(
  "org.apache.kafka" % "kafka-clients" % Versions.Kafka,
  "org.apache.kafka" % "connect-json" % Versions.Kafka,
)

excludeDependencies := Seq(
  "org.slf4j" % "slf4j-log4j12",
  "log4j",
  "org.apache.logging.log4j"
)

libraryDependencies ++= dependencies
