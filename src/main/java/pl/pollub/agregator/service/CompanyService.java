package pl.pollub.agregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pollub.agregator.dto.CompanyDTO;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.entity.Role;
import pl.pollub.agregator.entity.User;
import pl.pollub.agregator.repository.CompanyRepository;
import pl.pollub.agregator.repository.OfferRepository;
import pl.pollub.agregator.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CompanyDTO> getAll(boolean includeInactive) {
        List<Company> companies = includeInactive
                ? companyRepository.findAll()
                : companyRepository.findAllByIsActiveTrue();

        return companies.stream()
                .map(Company::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CompanyDTO> getById(Long id) {
        return companyRepository.findById(id)
                .map(Company::toDTO);
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getAllOffers(Long companyId, boolean includeInactive) {
        List<Offer> offers = includeInactive
                ? offerRepository.findAllByCompanyId(companyId)
                : offerRepository.findAllByCompanyIdAndIsActiveTrue(companyId);

        return offers.stream()
                .map(Offer::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return companyRepository.existsById(id);
    }

    @Transactional
    public CompanyDTO save(CompanyDTO dto, User currentUser) {
        Company company = Company.fromDTO(dto);
        company.setIsActive(true);

        if (currentUser != null) {
            if (currentUser.getRole() == Role.ADMIN && dto.getUserId() != null) {
                userRepository.findById(dto.getUserId()).ifPresent(company::setUser);
            } else {
                company.setUser(currentUser);
            }
        }

        Company saved = companyRepository.save(company);
        return saved.toDTO();
    }

    @Transactional
    public Optional<CompanyDTO> update(Long id, CompanyDTO dto, User currentUser) {
        return companyRepository.findById(id)
                .map(company -> {
                    verifyOwnership(company, currentUser);
                    company.restoreFromSnapshot(dto);
                    company.setId(id);

                    if (currentUser != null && currentUser.getRole() == Role.ADMIN && dto.getUserId() != null) {
                        userRepository.findById(dto.getUserId()).ifPresent(company::setUser);
                    }

                    return companyRepository.save(company).toDTO();
                });
    }

    @Transactional
    public boolean softDelete(Long id, User currentUser) {
        return companyRepository.findById(id)
                .map(company -> {
                    verifyOwnership(company, currentUser);
                    company.setIsActive(false);
                    companyRepository.save(company);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean hardDelete(Long id, User currentUser) {
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            return false;
        }
        verifyOwnership(companyOpt.get(), currentUser);
        companyRepository.deleteById(id);
        return true;
    }

    private void verifyOwnership(Company company, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Brak autoryzacji.");
        }
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (company.getUser() == null || !company.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Nie masz uprawnien do modyfikacji tej firmy.");
        }
    }
}
