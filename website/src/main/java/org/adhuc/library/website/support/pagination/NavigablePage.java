package org.adhuc.library.website.support.pagination;

import org.springframework.data.domain.Page;

import java.util.Optional;

public interface NavigablePage<T> extends Page<T> {

    boolean hasLink(String linkName);

    Optional<String> getLink(String linkName);

}
