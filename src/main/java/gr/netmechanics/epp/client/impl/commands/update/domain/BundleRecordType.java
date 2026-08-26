package gr.netmechanics.epp.client.impl.commands.update.domain;

import lombok.Getter;

@Getter
public enum BundleRecordType {
    DOMAIN("domain"),
    DNAME("dname");

    private final String xmlName;

    BundleRecordType(final String xmlName) {
        this.xmlName = xmlName;
    }
}
