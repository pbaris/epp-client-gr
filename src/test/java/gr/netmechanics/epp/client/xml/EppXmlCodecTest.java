package gr.netmechanics.epp.client.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.Hello;
import org.junit.jupiter.api.Test;

public class EppXmlCodecTest {

    @Test
    void unmarshal_wraps_parse_failures_in_epp_gateway_exception() {
        EppXmlCodec codec = new EppXmlCodec(new XmlMapper());

        assertThatThrownBy(() -> codec.unmarshal("not xml at all", EppCommandResponse.class))
            .isInstanceOf(EppGatewayException.class);
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
