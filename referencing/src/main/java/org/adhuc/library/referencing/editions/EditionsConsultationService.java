package org.adhuc.library.referencing.editions;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class EditionsConsultationService {

    private final EditionsRepository editionsRepository;

    public EditionsConsultationService(EditionsRepository editionsRepository) {
        this.editionsRepository = editionsRepository;
    }

    public Page<Edition> getPage(Pageable request) {
        return editionsRepository.findPage(request);
    }

}
