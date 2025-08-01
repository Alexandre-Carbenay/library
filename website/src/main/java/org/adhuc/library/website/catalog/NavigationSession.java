package org.adhuc.library.website.catalog;

import org.adhuc.library.website.support.pagination.NavigablePage;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Component
@Scope(
        value = SCOPE_SESSION,
        proxyMode = TARGET_CLASS
)
public class NavigationSession {

    @Nullable
    private NavigablePage<Book> currentPage;

    public Optional<NavigablePage<Book>> currentPage() {
        return Optional.ofNullable(currentPage);
    }

    public void switchPage(NavigablePage<Book> page) {
        this.currentPage = page;
    }

    public void clearCurrentPage() {
        this.currentPage = null;
    }
}
