import java.time.Duration
import java.util.Properties

import Producer.{RedditProducer, TwitterProducer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import scala.collection.JavaConverters._

object SocialMediaIngest extends App {
  val conf = ConfigFactory.load()

  val redditProducer = new Thread(new RedditProducer(conf))
  val twitterProducer = new Thread(new TwitterProducer(conf))
  redditProducer.start()
  twitterProducer.start()

  import Serdes._

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "social-media-ingest")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p
  }

  val builder: StreamsBuilder = new StreamsBuilder
  val socialMediaTopic = conf.getString("inputTopic")
  val rawMessages: KStream[String, String] = builder.stream[String, String](socialMediaTopic)
  val stopwords = conf.getStringList("stopwords").asScala.toList
  val cleanMessages = rawMessages
    // remove stop words
    .filterNot {
      case (_, s: String) => s == null | s == "" | s.isEmpty
      case _ => true
    }
    .mapValues((_, text) => {
      println(text)
      val result = text.split(" ")
        .filterNot(stopwords.contains(_))
        .foldLeft("")((res, word) => res + ' ' + word)
    result
  })

  cleanMessages.to("outputTopic")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}