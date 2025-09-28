package org.adhuc.library.referencing.authors;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class AuthorsConsultationService {

    private final AuthorsRepository authorsRepository;

    public AuthorsConsultationService(AuthorsRepository authorsRepository) {
        this.authorsRepository = authorsRepository;
    }

    public Page<Author> getPage(Pageable request) {
        return authorsRepository.findPage(request);
    }

}
