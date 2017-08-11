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
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

@EnableBinding({ Sink.class, KafkaPollingSink.Sinks.class })
public class KafkaPollingSink {

    private static Logger logger = LoggerFactory.getLogger(KafkaPollingSink.class);

    @Bean
    @InboundChannelAdapter(value = "output", poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
    public MessageSource<String> source() {
        return () -> {
            String value = "{\"value\":\"hi\"}";
            return MessageBuilder.withPayload(value).build();
        };
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
