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
package <%=packageName%>.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/")
@EnableBinding({ Source.class, KafkaController.Sinks.class })
public class KafkaController {

    private static Logger logger = LoggerFactory.getLogger(KafkaController.class);

    private MessageChannel output;

    public KafkaController(@Qualifier("output") MessageChannel output) {
        this.output = output;
    }

    @PostMapping("/person")
    public boolean createSchema(@RequestBody JsonNode node) {
        logger.debug("Received json {}", node);
        Message<JsonNode> message = MessageBuilder.withPayload(node).build();
        return output.send(message);
        //What happends if send returns true but there is a timing error later
    }

    @StreamListener("loggingIn")
    public void log(String message) {
        logger.info("Finished processing {}", message);
    }

    public interface Sinks {
        @Input
        SubscribableChannel loggingIn();
    }
}