# Social Media Stream Pipeline + Query

A demo application for a POC involving Analytics of Social Media contributions.
It consists of two repos, `SocialMediaStream_Pipeline` [(here)](https://github.com/mionisation/SocialMediaStream_Pipeline) and `SocialMediaStream_Query` [(here)](https://github.com/mionisation/SocialMediaStream_Query).

`SocialMediaStream_Pipeline` consumes messages from Twitter and Reddit, streams and processes them over Kafka and then saves them to Cassandra. 

`SocialMediaStream_Query` connects to Cassandra and executes a Spark Query that counts how many times specific keywords were mentioned in the posts.

Pipeline visualized:

![Imgur](https://imgur.com/oOzq2p2.png)

How to run `SocialMediaStream_Pipeline` locally:

- Dependencies
    - Download [Kafka binaries](https://kafka.apache.org/downloads)
    - Install [Cassandra](http://cassandra.apache.org/download/)

1. Import the project in IntelliJ + Scala support
2. Create Twitter API tokens [here](https://developer.twitter.com/en/apps) and Reddit API tokens [here](https://www.reddit.com/prefs/apps)
3. Put in API tokens in `application.conf`
4. Run Zookeeper:
    
    `bin/zookeeper-server-start.sh config/zookeeper.properties`
5. Run Kafka Broker:
    
    `bin/kafka-server-start.sh config/server.properties`
6. Create Kafka topics:
    
    `bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic inputTopic`

    `bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic outputTopic`
7. Create Cassandra keyspace
    ```
    CREATE KEYSPACE IF NOT EXISTS social
      WITH REPLICATION = { 
       'class' : 'SimpleStrategy', 
       'replication_factor' : 1 
      };
    ```

    and table

    `CREATE TABLE social.posts ( timestamp text PRIMARY KEY, post text );`
