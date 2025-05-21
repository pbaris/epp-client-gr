package gr.netmechanics.epp.client.impl.elements.cd;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.elements.Contact;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactCheckData {
    @JacksonXmlProperty(localName = "id")
    private Contact contact;

    @JacksonXmlProperty(localName = "reason")
    private String reason;
}
