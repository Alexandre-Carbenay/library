package org.adhuc.library.catalog.adapter.autoload;

import org.adhuc.library.catalog.authors.Author;

import java.time.LocalDate;
import java.util.UUID;

class AutoLoadedAuthorsMother {

    static final Author ALBERT_CAMUS = new Author(
            UUID.fromString("f921c511-6341-494a-8152-c92613db248b"),
            "Albert Camus",
            LocalDate.parse("1913-11-07"),
            LocalDate.parse("1960-01-04")
    );
    static final Author GUSTAVE_FLAUBERT = new Author(
            UUID.fromString("40ad7b89-7aec-45ae-bdd1-410e5a11f44b"),
            "Gustave Flaubert",
            LocalDate.parse("1821-12-12"),
            LocalDate.parse("1880-08-08")
    );
    static final Author LEON_TOLSTOI = new Author(
            UUID.fromString("2c98db8d-d0c3-4dfb-8cfe-355256300a91"),
            "Léon Tolstoï",
            LocalDate.parse("1828-08-28"),
            LocalDate.parse("1910-11-07")
    );
    static final Author VIRGINIE_DESPENTES = new Author(
            UUID.fromString("b61b590b-fdf8-436a-80bd-03c1591e482c"),
            "Virginie Despentes",
            LocalDate.parse("1969-06-13"),
            null
    );

}
