package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_DACOR;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_DACOR_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface DacorSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:dacor")
    default String xmlns() {
        return NS_DACOR;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_DACOR_LOC;
    }
}
