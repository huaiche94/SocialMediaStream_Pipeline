package Producer

import java.util.Properties

import com.typesafe.config.Config
import net.dean.jraw.http.{OkHttpNetworkAdapter, UserAgent}
import net.dean.jraw.models._
import net.dean.jraw.oauth.{Credentials, OAuthHelper}
import net.dean.jraw.references.CommentsRequest
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.JavaConverters._

class RedditProducer(conf: Config) extends Runnable {
  def run = {
    val props = new Properties()

    props.put("bootstrap.servers", conf.getString("kafkaServer"))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val topic = conf.getString("socialMediaTopic")

    val subs = conf.getStringList("reddit.keywords").asScala.toList

    // Assuming we have a 'script' reddit app// Assuming we have a 'script' reddit app
    val username = conf.getString("reddit.user.key")
    val password = conf.getString("reddit.user.secret")
    val clientId = conf.getString("reddit.client.key")
    val clientSecret = conf.getString("reddit.client.secret")

    val maxSubmissions = conf.getInt("reddit.maxSubmissions")
    val maxComments = conf.getInt("reddit.maxComments")
    val maxDepth = conf.getInt("reddit.maxDepth")

    val oauthCreds = Credentials.script(username, password, clientId, clientSecret)

    // Create a unique User-Agent for our bot
    val userAgent = new UserAgent("bot", "DataDemoA_Ingestion", "v0.1", username);

    // Authenticate our client
    val reddit = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds)

    subs.foreach(
      sub => {
        val paginator = reddit.subreddit(sub)
                              .posts()
                              .limit(maxSubmissions)
                              .sorting(SubredditSort.NEW)
                              .build()

        paginator.iterator().forEachRemaining(
          page => {
            page.getChildren.forEach(
              submission => {
                if(submission.isSelfPost) {
                  val date = submission.getCreated.toInstant.getEpochSecond
                  val text = submission.getSelfText
                  sendRecord(date, text)
                }
                val id = submission.getId
                val commentRoot = reddit.submission(id).comments(new CommentsRequest(null, null, maxDepth, maxComments, CommentSort.CONFIDENCE))
                if(commentRoot.totalSize() > 1) {
                  val iter = commentRoot.walkTree().iterator()
                  while ( {
                    iter.hasNext
                  }) { // A PublicContribution is either a Submission or a Comment.
                    val thing = iter.next.getSubject
                    // Do something with each Submission/Comment
                    try {
                      val date = thing.getEdited.toInstant.getEpochSecond
                      val text = thing.getBody
                      sendRecord(date, text)
                    } catch {
                      case _: NullPointerException => () // ignore deleted posts
                    }
                  }
                }
              }
            )
          }
        )
      }
    )

    def sendRecord(epoch: Long, text:String) = {
      val record = new ProducerRecord(topic, epoch.toString, text)
      producer.send(record)
    }
    producer.close()
  }
}
