package gr.netmechanics.epp.client.impl.commands.info.registrar;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.info.InfoResponse;
import gr.netmechanics.epp.client.impl.elements.ContactStatus;
import gr.netmechanics.epp.client.impl.elements.PostalInfo;
import gr.netmechanics.epp.client.impl.schema.RegistrarSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrarInfoResponse implements RegistrarSchema, InfoResponse {

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "roid")
    private String repositoryObjectId;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "status")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ContactStatus> statuses;

    @JacksonXmlProperty(localName = "postalInfo")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<PostalInfo> postalInfos;

    @JacksonXmlProperty(localName = "voice")
    private String voice;

    @JacksonXmlProperty(localName = "fax")
    private String fax;

    @JacksonXmlProperty(localName = "email")
    private String email;

    @JacksonXmlProperty(localName = "clID")
    private String clientId;

    @JacksonXmlProperty(localName = "crID")
    private String creatorId;

    @JacksonXmlProperty(localName = "crDate")
    private String creationDate;

    @JacksonXmlProperty(localName = "upID")
    private String updatorId;

    @JacksonXmlProperty(localName = "upDate")
    private String updateDate;
}
