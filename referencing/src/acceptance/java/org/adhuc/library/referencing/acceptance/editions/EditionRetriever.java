package org.adhuc.library.referencing.acceptance.editions;

import java.util.List;

import static org.adhuc.library.referencing.acceptance.editions.actions.EditionsListing.listEditions;

public class EditionRetriever {

    public static List<Edition> findEditionsByIsbn(String isbn) {
        var editions = listEditions();
        return editions.stream().filter(edition -> edition.hasIsbn(isbn)).toList();
    }

}
