package gr.netmechanics.epp.client.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import gr.netmechanics.epp.client.EppConstants;
import gr.netmechanics.epp.client.EppCookieInterceptor;
import gr.netmechanics.epp.client.EppPropertiesProvider;
import gr.netmechanics.epp.client.EppSessionCookieStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class EppHttpClientConfiguration {

    // Connect/read timeouts are fixed at bean-creation time. This is unlike the target URL, which
    // RestEppGateway deliberately re-reads via ApplicationContext.getBean(...) on every call so that
    // an EppRefreshEvent takes effect immediately without rebuilding this bean. Intentional, not a bug.
    @Bean
    @ConditionalOnMissingBean(name = EppConstants.BEAN_REST_CLIENT)
    public RestClient eppRestClient(final EppPropertiesProvider eppProps, final EppSessionCookieStore cookieStore) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(eppProps.getConnectTimeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(eppProps.getReadTimeoutMillis()));

        return RestClient.builder()
            .requestFactory(requestFactory)
            .requestInterceptor(new EppCookieInterceptor(cookieStore))
            .messageConverters(converters ->
                converters.addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8)))
            .build();
    }
}
