package org.adhuc.library.referencing.publishers;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class PublishersConsultationService {

    private final PublishersRepository publishersRepository;

    public PublishersConsultationService(PublishersRepository publishersRepository) {
        this.publishersRepository = publishersRepository;
    }

    public Page<Publisher> getPage(Pageable request) {
        return publishersRepository.findPage(request);
    }

}
