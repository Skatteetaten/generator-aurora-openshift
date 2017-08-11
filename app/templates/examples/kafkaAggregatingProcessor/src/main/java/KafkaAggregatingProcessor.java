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

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy;
import org.springframework.integration.aggregator.MessageCountReleaseStrategy;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.skatteetaten.aurora.filter.logging.AuroraHeaderFilter;

/*
This type of processor will aggregate together messages from several topic into one message.
This particular example aggregates from 2 topics, It will wait for 3seconds for the second message
to appear before releasing.

We use a simple in memory message store.

 */
@EnableBinding({ Processor.class, KafkaAggregatingProcessor.Sinks.class })
public class KafkaAggregatingProcessor {

    private static final long GROUP_TIMEOUT = 3000L;
    private static final long SEND_TIMEOUT = 1000L;
    private static final int CAPACITY = 10;
    private static Logger logger = LoggerFactory.getLogger(KafkaAggregatingProcessor.class);

    @ServiceActivator(inputChannel = "input")
    @Bean
    public AggregatingMessageHandler aggregator() {

        AggregatingMessageHandler aggregator =
            new AggregatingMessageHandler(new Joiner(new ObjectMapper()),
                new SimpleMessageStore(CAPACITY));
        aggregator.setOutputChannelName("output");
        aggregator.setSendPartialResultOnExpiry(true);
        aggregator.setSendTimeout(SEND_TIMEOUT);
        aggregator.setCorrelationStrategy(new HeaderAttributeCorrelationStrategy(AuroraHeaderFilter.KORRELASJONS_ID));
        aggregator.setReleaseStrategy(new MessageCountReleaseStrategy(2));
        aggregator.setExpireGroupsUponCompletion(true);
        aggregator.setGroupTimeoutExpression(new ValueExpression<>(GROUP_TIMEOUT));
        aggregator.setExpireGroupsUponTimeout(true);

        return aggregator;
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

    public class Joiner implements MessageGroupProcessor {

        private ObjectMapper mapper;

        public Joiner(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public List<?> processMessageGroup(MessageGroup messageGroup) {

            return messageGroup.getMessages()
                .stream()
                .map(Message::getPayload)
                .collect(Collectors.toList());
        }
    }
}
