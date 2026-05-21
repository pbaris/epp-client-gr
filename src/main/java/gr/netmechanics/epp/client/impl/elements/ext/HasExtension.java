package gr.netmechanics.epp.client.impl.elements.ext;

import gr.netmechanics.epp.client.impl.EppExtension;

import java.util.Collections;
import java.util.List;

public interface HasExtension {

    EppExtension getExtension();

    default List<EppExtension> getExtensions() {
        EppExtension extension = getExtension();
        return extension == null ? Collections.emptyList() : List.of(extension);
    }

}
