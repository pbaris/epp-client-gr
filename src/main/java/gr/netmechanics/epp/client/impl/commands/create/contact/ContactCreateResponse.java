package gr.netmechanics.epp.client.impl.commands.create.contact;

import java.time.LocalDate;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.create.CreateResponse;
import gr.netmechanics.epp.client.impl.schema.ContactSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactCreateResponse implements ContactSchema, CreateResponse {
    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "crDate")
    private LocalDate createdDate;
}
