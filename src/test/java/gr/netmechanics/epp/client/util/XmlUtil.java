package gr.netmechanics.epp.client.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.underscore.U;
import com.github.underscore.Xml;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * @author Panos Bariamis (pbaris)
 */
@Component
public class XmlUtil {

    private final XmlMapper xmlMapper;

    public XmlUtil(final XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
        this.xmlMapper.disable(SerializationFeature.INDENT_OUTPUT);
    }

    public String toXml(Object obj) throws Exception {
        return minifyXml(xmlMapper.writeValueAsString(obj));
    }

    public String xmlFromFile(final String filename) throws Exception {
        ClassPathResource resource = new ClassPathResource("xml/" + filename);
        return minifyXml(FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)));
    }

    private static String minifyXml(final String xml) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        StreamResult result = new StreamResult(new StringWriter());

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.transform(new StreamSource(inputStream), result);

        return U.formatXml(result.getWriter().toString(), Xml.XmlStringBuilder.Step.COMPACT);
    }
}
