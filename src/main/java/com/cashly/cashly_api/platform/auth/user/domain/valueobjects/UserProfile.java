package com.cashly.cashly_api.platform.auth.user.domain.valueobjects;

public record UserProfile(String firstName, String lastName, Address address,
        String phone) {
    public UserProfile {
        firstName = text(firstName, 100, true);
        lastName = text(lastName, 100, true);
        if (phone != null) {
            phone = phone.replaceAll("[\\s().-]", "");
            if (phone.isEmpty())
                phone = null;
            else if (!phone.matches("\\+?[0-9]{7,15}")) {
                throw new IllegalArgumentException("Invalid phone number");
            }
        }
    }

    public static String text(String value, int max, boolean required) {
        if (value == null) {
            if (required) {
                throw new IllegalArgumentException("Required text is missing");
            }
            return null;
        }
        value = value.strip();
        if ((required && value.isEmpty())
                || value.codePointCount(0, value.length()) > max) {
            throw new IllegalArgumentException(
                    "Invalid text length (maximum " + max + ")");
        }
        return value;
    }

    public record Address(String street, String city, String region,
            String country, String postalCode) {
        public Address {
            street = text(street, 200, false);
            city = text(city, 100, false);
            region = text(region, 100, false);
            country = text(country, 100, false);
            postalCode = text(postalCode, 20, false);
        }
    }

}
