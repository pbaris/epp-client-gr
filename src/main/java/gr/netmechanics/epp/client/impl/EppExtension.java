package gr.netmechanics.epp.client.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.netmechanics.epp.client.impl.elements.Comment;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface EppExtension {

    @JsonIgnore
    default List<Comment> getComments() {
        throw new UnsupportedOperationException("Comments are not supported for %s extension".formatted(getClass().getSimpleName()));
    }
}
