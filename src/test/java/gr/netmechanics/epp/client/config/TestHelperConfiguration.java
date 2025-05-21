package gr.netmechanics.epp.client.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gr.netmechanics.epp.client.util.XmlUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author Panos Bariamis (pbaris)
 */
@TestConfiguration
public class TestHelperConfiguration {
    @Bean
    public XmlUtil xmlUtil(final XmlMapper xmlMapper) {
        return new XmlUtil(xmlMapper);
    }
}
