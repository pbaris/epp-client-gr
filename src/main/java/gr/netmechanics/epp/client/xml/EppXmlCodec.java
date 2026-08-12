package gr.netmechanics.epp.client.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.underscore.U;
import com.github.underscore.Xml;
import gr.netmechanics.epp.client.error.EppGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EppXmlCodec {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?s)(<((?:\\w+:)?(?:pw|newPW))>).*?(</\\2>)");

    private final XmlMapper xmlMapper;

    public String marshal(final Object payload) {
        try {
            String xml = xmlMapper.writeValueAsString(payload);
            if (log.isDebugEnabled()) {
                log.debug("Sending message:\n{}\n", redact(xml));
            }
            return xml;

        } catch (Exception e) {
            throw new EppGatewayException("Failed to marshal XML", e);
        }
    }

    public <T> T unmarshal(final String payload, final Class<T> type) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Received message:\n{}\n", redact(minifyXml(payload)));
            }
            return xmlMapper.readValue(payload, type);

        } catch (Exception e) {
            throw new EppGatewayException("Failed to unmarshal XML", e);
        }
    }

    static String redact(final String xml) {
        return PASSWORD_PATTERN.matcher(xml).replaceAll("$1***REDACTED***$3");
    }

    @SneakyThrows
    private static String minifyXml(final String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        StreamResult result = new StreamResult(new StringWriter());

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.transform(new StreamSource(inputStream), result);

        return U.formatXml(result.getWriter().toString(), Xml.XmlStringBuilder.Step.COMPACT);
    }
}
