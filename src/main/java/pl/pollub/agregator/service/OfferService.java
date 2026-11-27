package pl.pollub.agregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.entity.Role;
import pl.pollub.agregator.entity.User;
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
    public OfferDTO save(OfferDTO dto, User currentUser) {
        if (dto.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Oferta musi byc przypisana do firmy (companyId).");
        }

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono firmy o podanym id."));

        verifyCompanyOwnership(company, currentUser);

        Offer offer = Offer.fromDTO(dto);
        offer.setActive(true);
        offer.setCompany(company);

        Offer saved = offerRepository.save(offer);
        return saved.toDTO();
    }

    @Transactional
    public Optional<OfferDTO> update(Long id, OfferDTO dto, User currentUser) {
        return offerRepository.findById(id)
                .map(offer -> {
                    verifyCompanyOwnership(offer.getCompany(), currentUser);
                    offer.restoreFromSnapshot(dto);
                    offer.setId(id);

                    if (dto.getCompanyId() != null) {
                        Company newCompany = companyRepository.findById(dto.getCompanyId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono firmy o podanym id."));
                        verifyCompanyOwnership(newCompany, currentUser);
                        offer.setCompany(newCompany);
                    }

                    return offerRepository.save(offer).toDTO();
                });
    }

    @Transactional
    public boolean softDelete(Long id, User currentUser) {
        return offerRepository.findById(id)
                .map(offer -> {
                    verifyCompanyOwnership(offer.getCompany(), currentUser);
                    offer.setActive(false);
                    offerRepository.save(offer);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean hardDelete(Long id, User currentUser) {
        Optional<Offer> offerOpt = offerRepository.findById(id);
        if (offerOpt.isEmpty()) {
            return false;
        }
        verifyCompanyOwnership(offerOpt.get().getCompany(), currentUser);
        offerRepository.deleteById(id);
        return true;
    }

    private void verifyCompanyOwnership(Company company, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Brak autoryzacji.");
        }
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (company == null || company.getUser() == null || !company.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Nie masz uprawnien do zarzadzania ofertami tej firmy.");
        }
    }
}
