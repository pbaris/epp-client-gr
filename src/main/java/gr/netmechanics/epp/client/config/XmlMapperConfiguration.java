package gr.netmechanics.epp.client.config;

import javax.xml.stream.XMLInputFactory;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import gr.netmechanics.epp.client.impl.elements.Address;
import gr.netmechanics.epp.client.impl.elements.AuthorizationInfo;
import gr.netmechanics.epp.client.impl.elements.PostalInfo;
import gr.netmechanics.epp.client.xml.AddressSerializer;
import gr.netmechanics.epp.client.xml.AuthorizationInfoSerializer;
import gr.netmechanics.epp.client.xml.NamespaceXmlFactory;
import gr.netmechanics.epp.client.xml.PostalInfoSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Panos Bariamis (pbaris)
 */
@Configuration
public class XmlMapperConfiguration {

    @Bean
    public XmlMapper xmlMapper() {
        WstxInputFactory inFactory = new WstxInputFactory();
        inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);

        WstxOutputFactory outFactory = new WstxOutputFactory();
        outFactory.setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, Boolean.TRUE);

        SimpleModule module = new SimpleModule();
        module.addSerializer(PostalInfo.class, new PostalInfoSerializer());
        module.addSerializer(Address.class, new AddressSerializer());
        module.addSerializer(AuthorizationInfo.class, new AuthorizationInfoSerializer());

        XmlMapper xmlMapper = new XmlMapper(new NamespaceXmlFactory(inFactory, outFactory));
        xmlMapper.registerModule(module);

        xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        xmlMapper.findAndRegisterModules();

        return xmlMapper;
    }
}
