package gr.netmechanics.epp.client.impl.elements.ext;

import java.util.Collections;
import java.util.List;

import gr.netmechanics.epp.client.impl.EppExtension;

public interface HasExtension {

    EppExtension getExtension();

    default List<EppExtension> getExtensions() {
        EppExtension extension = getExtension();
        return extension == null ? Collections.emptyList() : List.of(extension);
    }

}
