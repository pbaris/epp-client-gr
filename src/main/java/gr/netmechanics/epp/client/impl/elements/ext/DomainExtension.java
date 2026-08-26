package gr.netmechanics.epp.client.impl.elements.ext;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.impl.commands.update.domain.BundleRecordType;
import gr.netmechanics.epp.client.impl.commands.update.domain.UpdateOperation;
import gr.netmechanics.epp.client.impl.elements.Bundle;
import gr.netmechanics.epp.client.impl.elements.Comment;
import gr.netmechanics.epp.client.impl.schema.DomainExtSchema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DomainExtension implements DomainExtSchema, EppExtension {

    @JacksonXmlProperty(localName = "protocol")
    private String protocol;

    @JacksonXmlProperty(localName = "bundle")
    private Bundle bundle;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "comment")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Comment> comments;

    @JacksonXmlProperty(localName = "extdomain:op")
    private String operation;

    @JacksonXmlProperty(localName = "extdomain:chg")
    private Chg chg;

    public DomainExtension(final UpdateOperation operation) {
        this.operation = operation.getXmlName();
    }

    public DomainExtension(final List<RecordTypeChange> recordTypeChanges) {
        this.chg = new Chg(recordTypeChanges);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Chg {
        @JacksonXmlElementWrapper(localName = "extdomain:record")
        @JacksonXmlProperty(localName = "extdomain:recordType")
        private List<RecordTypeChange> recordTypes;
    }

    @Getter
    public static class RecordTypeChange {
        @JacksonXmlText
        private final String bundleName;

        @JacksonXmlProperty(isAttribute = true, localName = "type")
        private final String type;

        public RecordTypeChange(final String bundleName, final BundleRecordType type) {
            this.bundleName = bundleName;
            this.type = type.getXmlName();
        }
    }
}
