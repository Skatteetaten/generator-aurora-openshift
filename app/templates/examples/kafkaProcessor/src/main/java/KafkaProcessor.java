/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package <%=packageName%>;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;

import com.fasterxml.jackson.databind.JsonNode;

@EnableBinding({ Processor.class, KafkaProcessor.Sinks.class })
public class KafkaProcessor {

    private static Logger logger = LoggerFactory.getLogger(KafkaProcessor.class);

    @StreamListener("input")
    @SendTo("output")
    public Message<String> address(Message<JsonNode> message) {
        //do something with JsonNode or something else
        JsonNode person = message.getPayload();

        //if anything wrong happends here it will retry 3 times then send to error topic
        return MessageBuilder.withPayload("foo").build();
    }

    @StreamListener("loggingIn")
    public void log(String message) {
        logger.info("Finished processing {}", message);
    }

    @StreamListener("loggingErr")
    public void error(String error) {
        logger.warn("ERRORS:{}", error);
    }

    public interface Sinks {

        @Input
        SubscribableChannel loggingErr();

        @Input
        SubscribableChannel loggingIn();
    }
}
