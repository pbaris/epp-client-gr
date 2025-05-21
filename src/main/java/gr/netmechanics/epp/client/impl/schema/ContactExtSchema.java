package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_CONTACT;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_CONTACT_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface ContactExtSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:extcontact")
    default String xmlns() {
        return NS_EXT_CONTACT;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_EXT_CONTACT_LOC;
    }
}
