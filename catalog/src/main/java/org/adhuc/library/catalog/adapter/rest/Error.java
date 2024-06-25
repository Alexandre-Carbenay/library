package org.adhuc.library.catalog.adapter.rest;

import java.time.ZonedDateTime;

public record Error(ZonedDateTime timestamp, int status, String error, String description) {
}
