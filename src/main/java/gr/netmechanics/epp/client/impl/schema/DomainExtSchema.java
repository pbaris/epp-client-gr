package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_DOMAIN;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_DOMAIN_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface DomainExtSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:extdomain")
    default String xmlns() {
        return NS_EXT_DOMAIN;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_EXT_DOMAIN_LOC;
    }
}
