package org.adhuc.library.catalog.books;

import net.jqwik.api.Arbitrary;

import static net.jqwik.api.Arbitraries.strings;

public class IsbnGenerator {

    private static final String ISBN13_PREFIX = "978";

    public static Arbitrary<String> isbn13s() {
        return strings().numeric().ofLength(9)
                .map(nineDigits -> {
                    var isbnWithoutCheckDigit = ISBN13_PREFIX + nineDigits;
                    int checkDigit = calculateIsbn13CheckDigit(isbnWithoutCheckDigit);
                    return isbnWithoutCheckDigit + checkDigit;
                });
    }

    private static int calculateIsbn13CheckDigit(String isbn) {
        var sum = 0;
        for (int i = 0; i < isbn.length(); i++) {
            var digit = Character.getNumericValue(isbn.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = 10 - (sum % 10);
        return checkDigit == 10 ? 0 : checkDigit;
    }

}
