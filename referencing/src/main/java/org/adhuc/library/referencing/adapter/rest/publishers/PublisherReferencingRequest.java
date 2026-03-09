package org.adhuc.library.referencing.adapter.rest.publishers;

import jakarta.validation.constraints.NotBlank;

record PublisherReferencingRequest(@NotBlank String name) {
}
