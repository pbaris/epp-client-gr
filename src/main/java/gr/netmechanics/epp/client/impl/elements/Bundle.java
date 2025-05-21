package gr.netmechanics.epp.client.impl.elements;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import gr.netmechanics.epp.client.xml.BooleanDeserializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Bundle {

    @JacksonXmlProperty(localName = "bundleName")
    private BundleName bundleName;

    @ToString.Include(name = "name")
    public String getName() {
        return bundleName.getName();
    }

    @ToString.Include(name = "chargeable")
    public boolean isChargeable() {
        return Boolean.TRUE.equals(bundleName.getChargeable());
    }

    @ToString.Include(name = "recordType")
    public String getRecordType() {
        return bundleName.getRecordType();
    }

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static class BundleName {
        @JacksonXmlText
        private String name;

        @JsonDeserialize(using = BooleanDeserializer.class)
        @JacksonXmlProperty(isAttribute = true, localName = "chargeable")
        private Boolean chargeable;

        @JacksonXmlProperty(isAttribute = true, localName = "recordType")
        private String recordType;
    }
}
