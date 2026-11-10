package pl.pollub.agregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.repository.CompanyRepository;
import pl.pollub.agregator.repository.OfferRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<OfferDTO> getAll(boolean includeInactive, Long companyId) {
        if (companyId != null) {
            return getByCompanyId(companyId);
        }

        List<Offer> offers = includeInactive
                ? offerRepository.findAll()
                : offerRepository.findAllByIsActiveTrue();

        return offers.stream()
                .map(Offer::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getByCompanyId(Long companyId) {
        return offerRepository.findAllByCompanyIdAndIsActiveTrue(companyId)
                .stream()
                .map(Offer::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OfferDTO> getById(Long id) {
        return offerRepository.findById(id)
                .map(Offer::toDTO);
    }

    @Transactional
    public OfferDTO save(OfferDTO dto) {
        Offer offer = Offer.fromDTO(dto);
        offer.setActive(true);

        if (dto.getCompanyId() != null) {
            companyRepository.findById(dto.getCompanyId())
                    .ifPresent(offer::setCompany);
        }

        Offer saved = offerRepository.save(offer);
        return saved.toDTO();
    }

    @Transactional
    public Optional<OfferDTO> update(Long id, OfferDTO dto) {
        return offerRepository.findById(id)
                .map(offer -> {
                    offer.restoreFromSnapshot(dto);
                    offer.setId(id);

                    if (dto.getCompanyId() != null) {
                        companyRepository.findById(dto.getCompanyId())
                                .ifPresent(offer::setCompany);
                    } else {
                        offer.setCompany(null);
                    }

                    return offerRepository.save(offer).toDTO();
                });
    }

    @Transactional
    public boolean softDelete(Long id) {
        return offerRepository.findById(id)
                .map(offer -> {
                    offer.setActive(false);
                    offerRepository.save(offer);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean hardDelete(Long id) {
        if (offerRepository.existsById(id)) {
            offerRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
