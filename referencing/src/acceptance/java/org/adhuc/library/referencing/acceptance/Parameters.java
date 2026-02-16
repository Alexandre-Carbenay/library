package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.ParameterType;

import java.time.LocalDate;
import java.util.Arrays;

public class Parameters {

    @ParameterType("[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ ]*")
    public String authorName(String source) {
        return source;
    }

    @ParameterType("[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ ]*")
    public String bookTitle(String source) {
        return source;
    }

    @ParameterType("\\d{4}-\\d{2}-\\d{2}")
    public LocalDate date(String source) {
        return LocalDate.parse(source);
    }

    @ParameterType("\\d{4}")
    public int year(String source) {
        return Integer.parseInt(source);
    }

    @ParameterType("French|English|German")
    public String language(String source) {
        return Language.fromName(source).code;
    }

    private enum Language {
        FRENCH("French", "fr"),
        ENGLISH("English", "en"),
        GERMAN("German", "de");

        private final String name;
        private final String code;

        Language(String name, String code) {
            this.name = name;
            this.code = code;
        }

        static Language fromName(String name) {
            return Arrays.stream(Language.values())
                    .filter(language -> language.name.equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No language with name " + name));
        }
    }

}
