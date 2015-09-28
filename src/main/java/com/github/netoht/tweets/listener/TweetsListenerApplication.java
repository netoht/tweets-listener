package com.github.netoht.tweets.listener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import com.codahale.metrics.MetricRegistry;
import com.github.netoht.tweets.listener.domain.TweetMessage;
import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

@SpringBootApplication
public class TweetsListenerApplication implements CommandLineRunner {

    @Autowired
    private MailSender mailSender;

    @Autowired
    private Environment env;

    @Autowired
    private MetricRegistry metrics;

    @Value("${spring.boot.admin.nofify.to}")
    private String mailTo;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String consumerKey = System.getProperty("consumerKey");
    private static final String consumerSecret = System.getProperty("consumerSecret");
    private static final String token = System.getProperty("token");
    private static final String secret = System.getProperty("secret");

    public static void main(String[] args) {
        if (StringUtils.isBlank(consumerKey) ||
            StringUtils.isBlank(consumerSecret) ||
            StringUtils.isBlank(token) ||
            StringUtils.isBlank(secret)) {
            System.out.println("Usage:");
            System.out.println("Twitter Apps (https://apps.twitter.com/)");
            System.out.println("Create New App and Application Settings");
            System.out.println("Get your app infos");
            System.out.println("$ java -jar app.jar -DconsumerKey=000 -DconsumerSecret=000 -Dtoken=000 -Dsecret=000");
            System.exit(1);
        }

        SpringApplication.run(TweetsListenerApplication.class, args);
    }

    @Override
    public void run(String... arg0) throws Exception {

        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        List<Long> followings = Lists.newArrayList(237084831L); // @hardmob_promo
        hosebirdEndpoint.followings(followings);

        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        ClientBuilder builder = new ClientBuilder()
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);

        Client hosebirdClient = builder.build();
        hosebirdClient.connect();

        while (!hosebirdClient.isDone()) {
            String rawMessage = msgQueue.take();

            metrics.meter("received.messages").mark();

            log.info("received message={}", rawMessage);
            TweetMessage tweetMessage = new TweetMessage(rawMessage);
            String message = tweetMessage.getTextWithTerms(Arrays.asList("xbox", "one"));

            if (StringUtils.isNotBlank(message)) {
                metrics.meter("received.messages.ok").mark();
                sendMail(message);
            }
        }
    }

    private void sendMail(String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(mailTo);
            mailMessage.setReplyTo(env.getProperty("spring.mail.username"));
            mailMessage.setFrom(env.getProperty("spring.mail.username"));
            mailMessage.setSubject("XBOX ONE");
            mailMessage.setText(message);
            mailSender.send(mailMessage);
            metrics.meter("sent.mail.messages.ok").mark();
        } catch (Exception e) {
            metrics.meter("sent.mail.messages.error").mark();
            log.error("send mail error", e);
        }
    }
}
