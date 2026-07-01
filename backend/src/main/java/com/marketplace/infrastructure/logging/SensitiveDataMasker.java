package com.marketplace.infrastructure.logging;

public final class SensitiveDataMasker {

    private SensitiveDataMasker() {}

    /**
     * Mask an email address: show first 2 chars and domain only.
     * Example: jo***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return "***" + (atIndex >= 0 ? email.substring(atIndex) : "");
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    /**
     * Mask a phone number: show last 4 digits only.
     * Example: ******1234
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "***";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() <= 4) {
            return "****";
        }
        return "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
    }

    /**
     * Mask a document number: show first 2 and last 2 characters only.
     * Example: AB***XY
     */
    public static String maskDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            return "***";
        }
        if (documentNumber.length() <= 4) {
            return "****";
        }
        return documentNumber.substring(0, 2)
                + "*".repeat(documentNumber.length() - 4)
                + documentNumber.substring(documentNumber.length() - 2);
    }

    /**
     * Mask any generic sensitive string: show first and last char only.
     */
    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return "***";
        }
        if (value.length() <= 2) {
            return "**";
        }
        return value.charAt(0) + "*".repeat(value.length() - 2) + value.charAt(value.length() - 1);
    }
}
