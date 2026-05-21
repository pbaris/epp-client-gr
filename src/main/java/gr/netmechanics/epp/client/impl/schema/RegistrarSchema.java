package gr.netmechanics.epp.client.impl.schema;

import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_ACCOUNT;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_ACCOUNT_LOC;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public interface RegistrarSchema {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:account")
    default String xmlns() {
        return NS_ACCOUNT;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    default String xsiSchemaLocation() {
        return NS_ACCOUNT_LOC;
    }
}
