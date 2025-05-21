package gr.netmechanics.epp.client.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
 * @author Panos Bariamis (pbaris)
 */
public class NamespaceXmlFactory extends XmlFactory {

    public static final String NS_EPP = "urn:ietf:params:xml:ns:epp-1.0";
    public static final String NS_EPP_LOC = NS_EPP + " epp-1.0.xsd";

    public static final String NS_EPPCOM = "urn:ietf:params:xml:ns:eppcom-1.0";
    public static final String NS_EPPCOM_LOC = NS_EPPCOM + " eppcom-1.0.xsd";

    public static final String NS_DOMAIN = "urn:ietf:params:xml:ns:domain-1.0";
    public static final String NS_DOMAIN_LOC = NS_DOMAIN + " domain-1.0.xsd";

    public static final String NS_CONTACT = "urn:ietf:params:xml:ns:contact-1.0";
    public static final String NS_CONTACT_LOC = NS_CONTACT + " contact-1.0.xsd";

    public static final String NS_HOST = "urn:ietf:params:xml:ns:host-1.0";
    public static final String NS_HOST_LOC = NS_HOST + " host-1.0.xsd";

    public static final String NS_DACOR = "urn:ics-forth:params:xml:ns:dacor-1.0";
    public static final String NS_DACOR_LOC = NS_DACOR + " dacor-1.0.xsd";

    public static final String NS_ACCOUNT = "urn:ics-forth:params:xml:ns:account-1.1";
    public static final String NS_ACCOUNT_LOC = NS_ACCOUNT + " account-1.1.xsd";

    public static final String NS_SEC_DNS = "urn:ietf:params:xml:ns:secDNS-1.1";
    public static final String NS_SEC_DNS_LOC = NS_SEC_DNS + " secDNS-1.1.xsd";

    public static final String NS_EXT_COMMON = "urn:ics-forth:params:xml:ns:extcommon-1.0";
    public static final String NS_EXT_COMMON_LOC = NS_EXT_COMMON + " extcommon-1.0.xsd";

    public static final String NS_EXT_DOMAIN = "urn:ics-forth:params:xml:ns:extdomain-1.3";
    public static final String NS_EXT_DOMAIN_LOC = NS_EXT_DOMAIN + " extdomain-1.3.xsd";

    public static final String NS_EXT_CONTACT = "urn:ics-forth:params:xml:ns:extcontact-1.0";
    public static final String NS_EXT_CONTACT_LOC = NS_EXT_CONTACT + " extcontact-1.0.xsd";

    public static final String NS_EXT_HOST = "urn:ics-forth:params:xml:ns:exthost-1.0";
    public static final String NS_EXT_HOST_LOC = NS_EXT_HOST + " exthost-1.0.xsd";

    public static final String NS_EXT_SEC_DNS = "urn:ics-forth:params:xml:ns:extSecDNS-1.0";
    public static final String NS_EXT_SEC_DNS_LOC = NS_EXT_SEC_DNS + " extSecDNS-1.0.xsd";

    public static final String NS_EXT_GR_RLS = "urn:ics-forth:params:xml:ns:ext-gr-rls-1.0";
    public static final String NS_EXT_GR_RLS_LOC = NS_EXT_GR_RLS + " ext-gr-rls-1.0.xsd";

    private static final Map<String, String> NS_MAP = new HashMap<>();

    static {
        NS_MAP.put("epp", NS_EPP);
        NS_MAP.put("eppcom", NS_EPPCOM);
        NS_MAP.put("domain", NS_DOMAIN);
        NS_MAP.put("contact", NS_CONTACT);
        NS_MAP.put("host", NS_HOST);
        NS_MAP.put("dacor", NS_DACOR);
        NS_MAP.put("account", NS_ACCOUNT);
        NS_MAP.put("secDNS", NS_SEC_DNS);
        NS_MAP.put("ext-gr-rls", NS_EXT_GR_RLS);
        NS_MAP.put("extcommon", NS_EXT_COMMON);
        NS_MAP.put("extdomain", NS_EXT_DOMAIN);
        NS_MAP.put("extcontact", NS_EXT_CONTACT);
        NS_MAP.put("exthost", NS_EXT_HOST);
        NS_MAP.put("extSecDNS", NS_EXT_SEC_DNS);
    }

    public NamespaceXmlFactory(final XMLInputFactory xmlIn, final XMLOutputFactory xmlOut) {
        super(xmlIn, xmlOut);
    }

    @Override
    protected XMLStreamWriter _createXmlWriter(final IOContext ctx, final Writer w) throws IOException {
        XMLStreamWriter writer = super._createXmlWriter(ctx, w);
        try {
            writer.setDefaultNamespace(NS_MAP.get("epp"));
            for (Map.Entry<String, String> e : NS_MAP.entrySet()) {
                writer.setPrefix(e.getKey(), e.getValue());
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, null);
        }
        return writer;
    }
}
