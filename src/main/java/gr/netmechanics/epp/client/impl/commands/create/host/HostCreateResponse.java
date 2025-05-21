package gr.netmechanics.epp.client.impl.commands.create.host;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.create.CreateResponse;
import gr.netmechanics.epp.client.impl.elements.Audit;
import gr.netmechanics.epp.client.impl.schema.HostSchema;
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
public class HostCreateResponse implements HostSchema, CreateResponse {

    @JacksonXmlProperty(localName = "name")
    private String name;

    @JsonUnwrapped
    private Audit audit;
}
