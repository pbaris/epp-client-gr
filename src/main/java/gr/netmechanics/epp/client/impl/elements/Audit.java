package gr.netmechanics.epp.client.impl.elements;

import java.time.LocalDate;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
public class Audit {

    @JacksonXmlProperty(localName = "clID")
    private String sponsoringClientId;

    @JacksonXmlProperty(localName = "crID")
    private String createdClientId;

    @JacksonXmlProperty(localName = "crDate")
    private LocalDate createdDate;

    @JacksonXmlProperty(localName = "upID")
    private String updatedClientId;

    @JacksonXmlProperty(localName = "upDate")
    private LocalDate updatedDate;

    @JacksonXmlProperty(localName = "exDate")
    private LocalDate expirationDate;

    @JacksonXmlProperty(localName = "trDate")
    private LocalDate transferredDate;
}
