import java.time.Duration
import java.util.Properties

import Producer.{RedditProducer, TwitterProducer}
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConverters._

object SocialMediaIngest extends App {
  val conf = ConfigFactory.load();

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
  val socialMediaTopic = conf.getString("socialMediaTopic")
  val rawMessages: KStream[Long, String] = builder.stream[Long, String](socialMediaTopic)
  val stopwords = conf.getStringList("stopwords").asScala.toList
  val cleanMessages = rawMessages
    // remove stop words
    .mapValues(text => {
      var cleaned = text
      stopwords.foreach( stop => {
        cleaned = text.replaceAll(stop, "")
        }
      )
      cleaned
    })
  cleanMessages.to("WordsWithCountsTopic")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}