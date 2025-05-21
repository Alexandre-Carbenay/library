package org.adhuc.library.website.catalog;

import org.adhuc.library.website.support.pagination.NavigablePage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

@Controller
@RequestMapping("/catalog")
@SessionAttributes("booksPage")
public class CatalogController {

    private final CatalogClient catalogClient;
    private final NavigationSession navigationSession;

    // First and last links must be displayed only if there is respectively a previous or a next page
    // This is due to the fact that Spring HATEOAS considers that the first and last links must always be available in response
    private final List<PageAttribute> pageAttributes = List.of(
            new PageAttribute("first", List.of("first", "prev"), "firstPageLinkName"),
            new PageAttribute("prev", "previousPageLinkName"),
            new PageAttribute("next", "nextPageLinkName"),
            new PageAttribute("last", List.of("last", "next"), "lastPageLinkName")
    );

    CatalogController(CatalogClient catalogClient, NavigationSession navigationSession) {
        this.catalogClient = catalogClient;
        this.navigationSession = navigationSession;
    }

    @GetMapping
    public String catalog(Model model, @RequestParam(name = "link", required = false) String linkToFollow,
                          @RequestHeader(name = ACCEPT_LANGUAGE, required = false, defaultValue = "") String acceptLanguages) {
        try {
            return getBooksPage(linkToFollow, acceptLanguages)
                    .map(page -> browsePage(model, page))
                    .orElseGet(this::redirectToDefaultPage);
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{page}")
    public String catalog(Model model, @PathVariable(name = "page") int pageNumber,
                          @RequestHeader(name = ACCEPT_LANGUAGE, required = false, defaultValue = "") String acceptLanguages) {
        try {
            var page = catalogClient.listBooks(pageNumber, acceptLanguages);
            return browsePage(model, page);
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/books/{id}")
    public String bookDetail(Model model, @PathVariable String id,
                             @RequestHeader(name = ACCEPT_LANGUAGE, required = false, defaultValue = "") String acceptLanguages) {
        model.addAttribute("book", catalogClient.getBook(id, acceptLanguages));
        return "catalog/book-detail";
    }

    private Optional<NavigablePage<Book>> getBooksPage(String linkToFollow, String acceptLanguages) {
        if (linkToFollow != null) {
            // The navigation session may not find the current page if the server has been restarted
            return navigationSession.currentPage()
                    .map(currentPage -> catalogClient.listBooks(currentPage, linkToFollow, acceptLanguages));
        }
        return Optional.of(catalogClient.listBooks(acceptLanguages));
    }

    private String browsePage(Model model, NavigablePage<Book> booksPage) {
        navigationSession.switchPage(booksPage);
        fillModel(model, booksPage);
        return "catalog/root";
    }

    private void fillModel(Model model, NavigablePage<Book> booksPage) {
        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("pageUrl", "/catalog/" + booksPage.getNumber());
        pageAttributes.forEach(pageAttribute -> pageAttribute.addToModel(booksPage, model));
    }

    private String redirectToDefaultPage() {
        navigationSession.clearCurrentPage();
        return "redirect:/catalog";
    }

    private record PageAttribute(String linkName, List<String> neededLinks, String attributeName) {
        PageAttribute(String linkName, String attributeName) {
            this(linkName, List.of(linkName), attributeName);
        }

        void addToModel(NavigablePage<?> page, Model model) {
            if (neededLinks.stream().allMatch(page::hasLink)) {
                model.addAttribute(attributeName, linkName);
            }
        }
    }

}
