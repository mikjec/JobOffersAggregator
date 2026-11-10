package pl.pollub.agregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pollub.agregator.dto.CompanyDTO;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.repository.CompanyRepository;
import pl.pollub.agregator.repository.OfferRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final OfferRepository offerRepository;

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
    public CompanyDTO save(CompanyDTO dto) {
        Company company = Company.fromDTO(dto);
        company.setIsActive(true);
        Company saved = companyRepository.save(company);
        return saved.toDTO();
    }

    @Transactional
    public Optional<CompanyDTO> update(Long id, CompanyDTO dto) {
        return companyRepository.findById(id)
                .map(company -> {
                    company.restoreFromSnapshot(dto);
                    company.setId(id);
                    return companyRepository.save(company).toDTO();
                });
    }

    @Transactional
    public boolean softDelete(Long id) {
        return companyRepository.findById(id)
                .map(company -> {
                    company.setIsActive(false);
                    companyRepository.save(company);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean hardDelete(Long id) {
        if (companyRepository.existsById(id)) {
            companyRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
