package gr.netmechanics.epp.client.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.underscore.U;
import com.github.underscore.Xml;
import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.AbstractPayloadTransformer;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@RequiredArgsConstructor
public class XMLToObjectTransformer extends AbstractPayloadTransformer<String, Object> {

    private final XmlMapper xmlMapper;

    @Override
    protected Object transformPayload(final String payload) {
        if (log.isDebugEnabled()) {
            log.debug("Received message:\n{}\n", minifyXml(payload));
        }

        try {
            return xmlMapper.readValue(payload, getReturnType(payload));

        } catch (Exception e) {
            throw new EppGatewayException("Failed to unmarshal XML", e);
        }
    }

    private Class<?> getReturnType(final String payload) {
        if (payload.contains("</greeting>")) {
            return Greeting.class;
        }

        if (payload.contains("</response>")) {
            return EppCommandResponse.class;
        }

        return null;
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
