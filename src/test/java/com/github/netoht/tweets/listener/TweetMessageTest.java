package com.github.netoht.tweets.listener;

import static org.hamcrest.Matchers.isEmptyString;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.github.netoht.tweets.listener.domain.TweetMessage;

public class TweetMessageTest {

    @Test
    public void test() {
        String rawMessage = "roteador 300mbps d-link clubedoricardo R$65,00 http://t.co/cfCCRCgXdi";
        TweetMessage tweetMessage = new TweetMessage(rawMessage);
        String message = tweetMessage.getTextWithTerms(Arrays.asList("xbox", "one"));
        Assert.assertThat(message, isEmptyString());
    }
}
