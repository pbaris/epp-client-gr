package gr.netmechanics.epp.client.impl.elements.ext;

import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_SEC_DNS;
import static gr.netmechanics.epp.client.xml.NamespaceXmlFactory.NS_SEC_DNS_LOC;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppExtension;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecDnsExtension implements EppExtension {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:secDNS")
    private final String xmlns = NS_SEC_DNS;

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    private final String xsiSchemaLocation = NS_SEC_DNS_LOC;

    @JacksonXmlProperty(localName = "secDNS:add")
    private DsNode add;

    @JacksonXmlProperty(localName = "secDNS:rem")
    private DsNode rem;

    @JacksonXmlProperty(localName = "secDNS:chg")
    private DsNode chg;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class DsNode {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "secDNS:dsData")
        private List<DsData> dsData;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class DsData {
        @JacksonXmlProperty(localName = "secDNS:keyTag")
        private int keyTag;

        @JacksonXmlProperty(localName = "secDNS:alg")
        private int alg;

        @JacksonXmlProperty(localName = "secDNS:digestType")
        private int digestType;

        @JacksonXmlProperty(localName = "secDNS:digest")
        private String digest;
    }
}
