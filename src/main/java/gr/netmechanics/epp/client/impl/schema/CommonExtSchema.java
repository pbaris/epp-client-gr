package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_COMMON;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EXT_COMMON_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface CommonExtSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:extcommon")
    default String xmlns() {
        return NS_EXT_COMMON;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_EXT_COMMON_LOC;
    }
}
