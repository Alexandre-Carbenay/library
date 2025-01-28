package org.adhuc.library.website.support.pagination;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

public class NavigablePageImpl<T> extends PageImpl<T> implements NavigablePage<T> {

    private final List<Link> links;

    public NavigablePageImpl(List<T> content, Pageable pageable, long total, List<Link> links) {
        super(content, pageable, total);
        this.links = links != null ? List.copyOf(links) : List.of();
    }

    @Override
    public boolean hasLink(String linkName) {
        Assert.hasText(linkName, "Link name must be set");
        return links.stream()
                .anyMatch(link -> link.correspondsTo(linkName));
    }

    @Override
    public Optional<String> getLink(String linkName) {
        Assert.hasText(linkName, "Link name must be set");
        return links.stream()
                .filter(link -> link.correspondsTo(linkName))
                .map(Link::href)
                .findFirst();
    }

    public record Link(String name, String href) {
        boolean correspondsTo(String name) {
            return this.name.equals(name);
        }
    }

}
