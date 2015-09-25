package com.github.netoht.tweets.listener;

import static org.apache.commons.lang3.StringUtils.stripAccents;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TweetMessage {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String rawMessage;
    private final String rawText;

    public TweetMessage(String message) {
        this.rawMessage = message;
        this.rawText = getRawText(message);
    }

    private String getRawText(String message) {
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(message);
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }
        if (Objects.isNull(jsonNode)) {
            return StringUtils.EMPTY;
        }
        JsonNode textField = jsonNode.get("text");
        if (Objects.isNull(textField) || StringUtils.isBlank(textField.asText())) {
            return StringUtils.EMPTY;
        }
        return textField.asText();
    }

    public String getTextWithTerms(List<String> terms) {
        String cleanText = stripAccents(rawText);
        if (checkTermsAndPrecedence(rawText, cleanText, terms)) {
            log.info("found terms={}, in text={}", terms, cleanText);
            return rawText;
        }
        log.info("not found terms={}, in text={}", terms, cleanText);
        return StringUtils.EMPTY;
    }

    private boolean checkTermsAndPrecedence(String rawText, String cleanText, List<String> terms) {
        for (int i = 0, indexOld = -1, index = -1; i < terms.size(); i++) {
            String term = terms.get(i);
            index = cleanText.indexOf(term);

            if (terms.size() == 1 && index > -1) {
                return true;
            }
            if (i == 0) {
                indexOld = index;
                continue;
            }
            if (index < indexOld) {
                return false;
            }
            if (i + 1 == terms.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
