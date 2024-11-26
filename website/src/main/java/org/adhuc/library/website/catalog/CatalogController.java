package org.adhuc.library.website.catalog;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogController {

    private final CatalogClient catalogClient;

    CatalogController(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @GetMapping("/catalog")
    public String catalog(Model model) {
        var booksPage = catalogClient.listBooks();
        model.addAttribute("books", booksPage.getContent());
        return "catalog/root";
    }

}
