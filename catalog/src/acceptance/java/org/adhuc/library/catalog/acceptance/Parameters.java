package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.ParameterType;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Parameters {

    @ParameterType(".*")
    public List<String> navigation(String source) {
        return List.of(source.split(", "));
    }

    @ParameterType(".*")
    public List<String> isbns(String source) {
        return List.of(source.split(", "));
    }

    @ParameterType(".*")
    public List<String> authorNames(String source) {
        return List.of(source.split(", "));
    }

    @ParameterType("[A-Za-z]*")
    public Locale language(String source) {
        return switch (source) {
            case "French" -> Locale.FRENCH;
            case "English" -> Locale.ENGLISH;
            case "German" -> Locale.GERMAN;
            default -> throw new IllegalArgumentException("Unknown language " + source);
        };
    }

    @ParameterType("[0-9a-f]{8}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{12}")
    public UUID uuid(String source) {
        return UUID.fromString(source);
    }

    @ParameterType("\\d{4}-\\d{2}-\\d{2}|<none>")
    public LocalDate date(String source) {
        return !"<none>".equals(source) ? LocalDate.parse(source) : null;
    }

}
