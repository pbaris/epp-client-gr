package gr.netmechanics.epp.client.impl.commands.delete.domain;

import lombok.Getter;

@Getter
public enum DeleteOperation {
    DOMAIN("deleteDomain"),
    HOMOGRAPH("deleteHomograph"),
    RECALL("recallApplication");

    private final String xmlName;

    DeleteOperation(final String xmlName) {
        this.xmlName = xmlName;
    }
}
