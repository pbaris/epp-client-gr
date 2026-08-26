package gr.netmechanics.epp.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gr.netmechanics.epp.client.impl.elements.Contact;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public interface EppBuilder {

    <T> T build();

    static <T> T requireNonNull(final T obj, final String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    static String requireNonEmpty(final String value, final String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static List<String> requireNonEmpty(final List<String> values, final String message) {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException(message);
        }
        return values;
    }

    static String requireNonEmptyMax(final String value, final int max, final String message) {
        String nonEmptyValue = requireNonEmpty(value, message);

        if (nonEmptyValue.length() > max) {
            throw new IllegalArgumentException(message);
        }
        return nonEmptyValue;
    }

    static List<String> requireNonEmptyMax(final List<String> values, final int max, final String message) {
        List<String> nonEmptyValues = requireNonEmpty(values, message);

        if (nonEmptyValues.size() > max) {
            throw new IllegalArgumentException(message);
        }
        return nonEmptyValues;
    }

    static int requireYears(final int years) {
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
