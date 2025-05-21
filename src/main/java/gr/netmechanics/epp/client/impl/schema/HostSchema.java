package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_HOST;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_HOST_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface HostSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:host")
    default String xmlns() {
        return NS_HOST;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_HOST_LOC;
    }
}
