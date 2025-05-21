package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_DOMAIN;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_DOMAIN_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface DomainSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:domain")
    default String xmlns() {
        return NS_DOMAIN;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_DOMAIN_LOC;
    }
}
