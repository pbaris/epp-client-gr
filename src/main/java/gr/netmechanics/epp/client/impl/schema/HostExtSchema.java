package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_HOST;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_HOST_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface HostExtSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:exthost")
    default String xmlns() {
        return NS_EXT_HOST;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_EXT_HOST_LOC;
    }
}
