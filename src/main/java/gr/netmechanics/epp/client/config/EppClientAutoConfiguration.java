package gr.netmechanics.epp.client.config;

import static gr.netmechanics.epp.client.EppConstants.BASE_PACKAGE;

import gr.netmechanics.epp.client.EppProperties;
import gr.netmechanics.epp.client.EppPropertiesDefaultProvider;
import gr.netmechanics.epp.client.EppPropertiesProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.annotation.IntegrationComponentScan;

/**
 * @author Panos Bariamis (pbaris)
 */
@AutoConfiguration
@ComponentScan(BASE_PACKAGE)
@ConfigurationPropertiesScan(BASE_PACKAGE)
@IntegrationComponentScan(BASE_PACKAGE)
public class EppClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EppPropertiesProvider.class)
    public EppPropertiesProvider eppPropertiesProvider(final EppProperties properties) {
        return new EppPropertiesDefaultProvider(properties);
    }
}
