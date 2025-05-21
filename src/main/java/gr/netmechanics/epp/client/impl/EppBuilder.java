package gr.netmechanics.epp.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gr.netmechanics.epp.client.impl.elements.Contact;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface EppBuilder {

    <T> T build();

    static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    static String requireNonEmpty(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static List<String> requireNonEmpty(List<String> values, String message) {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException(message);
        }
        return values;
    }

    static String requireNonEmptyMax(String value, int max, String message) {
        String nonEmptyValue = requireNonEmpty(value, message);

        if (nonEmptyValue.length() > max) {
            throw new IllegalArgumentException(message);
        }
        return nonEmptyValue;
    }

    static List<String> requireNonEmptyMax(List<String> values, int max, String message) {
        List<String> nonEmptyValues = requireNonEmpty(values, message);

        if (nonEmptyValues.size() > max) {
            throw new IllegalArgumentException(message);
        }
        return nonEmptyValues;
    }

    static int requireYears(int years) {
        if (years % 2 != 0 || years < 2 || years > 10) {
            throw new IllegalArgumentException("Renewal years must be an even number between 2 and 10");
        }

        return years;
    }

    static List<Contact> mergeContacts(final List<String> admin, final List<String> tech, final List<String> billing) {
        List<Contact> contacts = new ArrayList<>();

        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(admin)) {
            admin.stream()
                .map(id -> new Contact(requireNonEmpty(id, "Admin contact ID must be specified"), "admin"))
                .collect(Collectors.toCollection(() -> contacts));
        }

        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(tech)) {
            tech.stream()
                .map(id -> new Contact(requireNonEmpty(id, "Tech contact ID must be specified"), "tech"))
                .collect(Collectors.toCollection(() -> contacts));
        }

        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(billing)) {
            billing.stream()
                .map(id -> new Contact(requireNonEmpty(id, "Billing contact ID must be specified"), "billing"))
                .collect(Collectors.toCollection(() -> contacts));
        }

        return contacts;
    }
}
