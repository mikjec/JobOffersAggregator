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
    public List<OfferDTO> getAll() {
        return getAll(false, null);
    }

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
        validateId(companyId);
        return offerRepository.findAllByCompanyIdAndIsActiveTrue(companyId)
                .stream()
                .map(Offer::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OfferDTO> getById(Long id) {
        validateId(id);
        return offerRepository.findById(id)
                .map(Offer::toDTO);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id);
        return offerRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long countActiveByCompanyId(Long companyId) {
        validateId(companyId);
        return offerRepository.countByCompanyIdAndIsActiveTrue(companyId);
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> searchByLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location must not be blank.");
        }
        return offerRepository.findAllByLocationContainingIgnoreCaseAndIsActiveTrue(location.trim())
                .stream()
                .map(Offer::toDTO)
                .toList();
    }

    @Transactional
    public OfferDTO save(OfferDTO dto, User currentUser) {
        validateDto(dto);
        if (dto.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer must be assigned to a company (companyId).");
        }
        validateId(dto.getCompanyId());

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company with the given id was not found."));

        verifyCompanyOwnership(company, currentUser);

        Offer offer = Offer.fromDTO(dto);
        offer.setActive(true);
        offer.setCompany(company);

        Offer saved = offerRepository.save(offer);
        return saved.toDTO();
    }

    @Transactional
    public Optional<OfferDTO> update(Long id, OfferDTO dto, User currentUser) {
        validateId(id);
        validateDto(dto);
        return offerRepository.findById(id)
                .map(offer -> {
                    verifyCompanyOwnership(offer.getCompany(), currentUser);
                    offer.restoreFromSnapshot(dto);
                    offer.setId(id);

                    if (dto.getCompanyId() != null) {
                        validateId(dto.getCompanyId());
                        Company newCompany = companyRepository.findById(dto.getCompanyId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company with the given id was not found."));
                        verifyCompanyOwnership(newCompany, currentUser);
                        offer.setCompany(newCompany);
                    }

                    return offerRepository.save(offer).toDTO();
                });
    }

    @Transactional
    public boolean softDelete(Long id, User currentUser) {
        validateId(id);
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
        validateId(id);
        Optional<Offer> offerOpt = offerRepository.findById(id);
        if (offerOpt.isEmpty()) {
            return false;
        }
        verifyCompanyOwnership(offerOpt.get().getCompany(), currentUser);
        offerRepository.deleteById(id);
        return true;
    }

    @Transactional
    public boolean restore(Long id, User currentUser) {
        validateId(id);
        return offerRepository.findById(id)
                .map(offer -> {
                    verifyCompanyOwnership(offer.getCompany(), currentUser);
                    offer.setActive(true);
                    offerRepository.save(offer);
                    return true;
                })
                .orElse(false);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Identifier must be positive.");
        }
    }

    private void validateDto(OfferDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Offer data must not be null.");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Offer title must not be blank.");
        }
    }

    private void verifyCompanyOwnership(Company company, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Unauthorized.");
        }
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (company == null || company.getUser() == null || !company.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage offers for this company.");
        }
    }
}
