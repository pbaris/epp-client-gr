package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_CONTACT;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_CONTACT_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface ContactSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:contact")
    default String xmlns() {
        return NS_CONTACT;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_CONTACT_LOC;
    }
}
