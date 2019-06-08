package Producer

import scala.collection.JavaConverters._
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.config.{Config, ConfigList}
import java.util.Properties
import org.apache.kafka.clients.producer._

class TwitterProducer(conf: Config) extends Runnable {
  def run = {

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val topic="test"

    val streamingClient = TwitterStreamingClient()
    val trackedWords = conf.getStringList("keywords").asScala.toList

    streamingClient.filterStatuses(tracks = trackedWords) {
      case tweet: Tweet =>
        val epoch = tweet.created_at.getEpochSecond
        val text = tweet.text
        val record = new ProducerRecord(topic, epoch.toString, text)
        producer.send(record)
    }
    producer.close()
  }
}
