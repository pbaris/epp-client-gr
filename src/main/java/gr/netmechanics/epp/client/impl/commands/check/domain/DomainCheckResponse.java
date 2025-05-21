package gr.netmechanics.epp.client.impl.commands.check.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.check.CheckResponse;
import gr.netmechanics.epp.client.impl.elements.cd.DomainCheckData;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
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
public class DomainCheckResponse implements DomainSchema, CheckResponse {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "cd")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<DomainCheckData> checkData;

}
