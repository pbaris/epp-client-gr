package gr.netmechanics.epp.client.config;

import java.time.Duration;

import gr.netmechanics.epp.client.EppCookieInterceptor;
import gr.netmechanics.epp.client.EppPropertiesProvider;
import gr.netmechanics.epp.client.EppSessionCookieStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class EppHttpClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClient eppRestClient(final EppPropertiesProvider eppProps, final EppSessionCookieStore cookieStore) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(eppProps.getConnectTimeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(eppProps.getReadTimeoutMillis()));

        return RestClient.builder()
            .requestFactory(requestFactory)
            .requestInterceptor(new EppCookieInterceptor(cookieStore))
            .build();
    }
}
