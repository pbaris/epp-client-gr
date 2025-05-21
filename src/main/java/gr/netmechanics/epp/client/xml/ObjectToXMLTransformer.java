package gr.netmechanics.epp.client.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.AbstractPayloadTransformer;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@RequiredArgsConstructor
public class ObjectToXMLTransformer extends AbstractPayloadTransformer<Object, String> {
    private final XmlMapper xmlMapper;

    @Override
    protected String transformPayload(final Object payload) {
        try {
            String xml = xmlMapper.writeValueAsString(payload);
            if (log.isDebugEnabled()) {
                log.debug("Sending message:\n{}\n", xml);
            }
            return xml;

        } catch (Exception e) {
            log.error("Failed to marshal XML: {}", e.getMessage());
            return payload.toString();
        }
    }
}
