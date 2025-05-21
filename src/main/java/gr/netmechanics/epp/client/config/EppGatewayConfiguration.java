package gr.netmechanics.epp.client.config;

import static gr.netmechanics.epp.client.EppConstants.BEAN_RESPONSE_FLOW;
import static gr.netmechanics.epp.client.EppConstants.BEAN_SHARED_HEADERS;
import static gr.netmechanics.epp.client.EppConstants.HTTP_REQUEST_CHANNEL;
import static gr.netmechanics.epp.client.EppConstants.HTTP_RESPONSE_CHANNEL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gr.netmechanics.epp.client.xml.XMLToObjectTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@Configuration
@EnableIntegration
public class EppGatewayConfiguration {

    @Bean(name = HTTP_REQUEST_CHANNEL)
    public MessageChannel httpRequestChannel() {
        return new DirectChannel();
    }

    @Bean(name = HTTP_RESPONSE_CHANNEL)
    public MessageChannel httpResponseChannel() {
        return new DirectChannel();
    }

    @Bean(name = BEAN_SHARED_HEADERS)
    public Map<String, Object> sharedHeaders() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    @GlobalChannelInterceptor(patterns = HTTP_REQUEST_CHANNEL)
    public ChannelInterceptor requestHeadersInterceptor(@Qualifier(BEAN_SHARED_HEADERS) final Map<String, Object> sharedHeaders) {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull final Message<?> message, @NonNull final MessageChannel channel) {
                if (sharedHeaders.containsKey("Cookie")) {
                    Map<String, Object> headers = new HashMap<>(message.getHeaders());
                    headers.put("Cookie", sharedHeaders.get("Cookie"));
                    return MessageBuilder.withPayload(message.getPayload()).copyHeaders(headers).build();
                }
                return message;
            }
        };
    }

    @Bean
    @GlobalChannelInterceptor(patterns = HTTP_RESPONSE_CHANNEL)
    public ChannelInterceptor responseHeadersInterceptor(@Qualifier(BEAN_SHARED_HEADERS) final Map<String, Object> sharedHeaders) {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull final Message<?> message, @NonNull final MessageChannel channel) {
                MessageHeaders headers = message.getHeaders();
                if (headers.containsKey("Set-Cookie")) {
                    sharedHeaders.put("Cookie", headers.get("Set-Cookie"));
                }

                return message;
            }
        };
    }

    @Bean(BEAN_RESPONSE_FLOW)
    public IntegrationFlow responseFlow(final XmlMapper xmlMapper) {
        return IntegrationFlow.from(HTTP_RESPONSE_CHANNEL)
            .transform(new XMLToObjectTransformer(xmlMapper))
            .get();
    }
}
