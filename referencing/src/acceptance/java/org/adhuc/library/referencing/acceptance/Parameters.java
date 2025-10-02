package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.ParameterType;

import java.time.LocalDate;

public class Parameters {

    @ParameterType("[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ ]*")
    public String authorName(String source) {
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

}
