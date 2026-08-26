package gr.netmechanics.epp.client.impl.elements.ext;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.netmechanics.epp.client.impl.EppExtension;

public interface HasExtension {

    @JsonIgnore
    EppExtension getExtension();

    @JsonIgnore
    default List<EppExtension> getExtensions() {
        EppExtension extension = getExtension();
        return extension == null ? Collections.emptyList() : List.of(extension);
    }

}
