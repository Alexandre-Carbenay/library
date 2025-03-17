package org.adhuc.library.catalog.editions;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@ApplicationServiceRing
public class CatalogService {

    private final EditionsRepository repository;

    CatalogService(EditionsRepository repository) {
        this.repository = repository;
    }

    public Page<Edition> getPage(Pageable request) {
        Assert.notNull(request, "Cannot get catalog page from null request");
        return repository.find(request);
    }

}
