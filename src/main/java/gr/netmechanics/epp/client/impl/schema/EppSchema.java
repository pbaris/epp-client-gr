package gr.netmechanics.epp.client.impl.schema;


import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EPP;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_EPP_LOC;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author Panos Bariamis (pbaris)
 */
@JsonPropertyOrder({ "xmlns", "xmlnsXsi", "xsiSchemaLocation" })
@JacksonXmlRootElement(localName = "epp")
public interface EppSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns")
    default String xmlns() {
        return NS_EPP;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
    default String xmlnsXsi() {
        return "http://www.w3.org/2001/XMLSchema-instance";
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_EPP_LOC;
    }

}
