package gr.netmechanics.epp.client.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Collectors;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.Hello;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class EppXmlCodecTest {

    @Test
    void unmarshal_wraps_parse_failures_in_epp_gateway_exception() {
        EppXmlCodec codec = new EppXmlCodec(new XmlMapper());

        assertThatThrownBy(() -> codec.unmarshal("not xml at all", EppCommandResponse.class))
            .isInstanceOf(EppGatewayException.class);
    }

    @Test
    void unmarshal_redacts_password_in_response_log() {
        Logger logger = (Logger) LoggerFactory.getLogger(EppXmlCodec.class);
        logger.setLevel(Level.DEBUG);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        EppXmlCodec codec = new EppXmlCodec(new XmlMapper());
        String xml = "<epp><response><resData><domain:infData xmlns:domain=\"urn:ietf:params:xml:ns:domain-1.0\">"
            + "<domain:authInfo><domain:pw>topsecret</domain:pw></domain:authInfo>"
            + "</domain:infData></resData></response></epp>";

        try {
            codec.unmarshal(xml, EppCommandResponse.class);
        } catch (EppGatewayException ignored) {
            // this minimal fixture may not fully match EppCommandResponse's shape;
            // we only care that the received-message log line was already redacted.
        } finally {
            logger.detachAppender(appender);
        }

        String logged = appender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .collect(Collectors.joining("\n"));

        assertThat(logged)
            .contains("***REDACTED***")
            .doesNotContain("topsecret");
    }

    @Test
    void redact_masks_password_field_but_preserves_everything_else() {
        String xml = "<command><login><clID>user</clID><pw><![CDATA[secret123]]></pw></login></command>";

        String redacted = EppXmlCodec.redact(xml);

        assertThat(redacted)
            .contains("<clID>user</clID>")
            .contains("<pw>***REDACTED***</pw>")
            .doesNotContain("secret123");
    }

    @Test
    void redact_masks_new_password_field_too() {
        String xml = "<login><newPW>brandNewSecret</newPW></login>";

        String redacted = EppXmlCodec.redact(xml);

        assertThat(redacted)
            .contains("<newPW>***REDACTED***</newPW>")
            .doesNotContain("brandNewSecret");
    }

    @Test
    void redact_masks_namespace_prefixed_password_field() {
        String xml = "<domain:transfer><domain:authInfo><domain:pw>password</domain:pw>"
            + "</domain:authInfo></domain:transfer>";

        String redacted = EppXmlCodec.redact(xml);

        assertThat(redacted)
            .contains("<domain:pw>***REDACTED***</domain:pw>")
            .doesNotContain("password");
    }

    @Test
    void redact_masks_namespace_prefixed_new_password_field() {
        String xml = "<contact:update><contact:authInfo><contact:newPW>brandNewSecret</contact:newPW>"
            + "</contact:authInfo></contact:update>";

        String redacted = EppXmlCodec.redact(xml);

        assertThat(redacted)
            .contains("<contact:newPW>***REDACTED***</contact:newPW>")
            .doesNotContain("brandNewSecret");
    }

    @Test
    void redact_does_not_collapse_mismatched_open_and_close_tag_names() {
        String xml = "<foo><pw>x</newPW></foo>";

        String redacted = EppXmlCodec.redact(xml);

        // "<pw>x</newPW>" has a mismatched close tag name and must not be treated as a valid pw block.
        assertThat(redacted).isEqualTo(xml);
    }

    @Test
    void redact_is_a_no_op_when_there_is_no_password_field() {
        String xml = "<hello/>";

        assertThat(EppXmlCodec.redact(xml)).isEqualTo("<hello/>");
    }

    @Test
    void marshal_serializes_object_to_xml_string() {
        EppXmlCodec codec = new EppXmlCodec(new XmlMapper());

        String xml = codec.marshal(new Hello());

        assertThat(xml)
            .isNotNull()
            .isNotEmpty()
            .contains("<hello");
    }

    @Test
    void marshal_wraps_serialization_failures_in_epp_gateway_exception() {
        EppXmlCodec codec = new EppXmlCodec(new XmlMapper());

        class Unserializable {
            public String getValue() {
                throw new RuntimeException("boom");
            }
        }

        assertThatThrownBy(() -> codec.marshal(new Unserializable()))
            .isInstanceOf(EppGatewayException.class);
    }
}
