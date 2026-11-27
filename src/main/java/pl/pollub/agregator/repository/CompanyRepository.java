package pl.pollub.agregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.pollub.agregator.entity.Company;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findAllByIsActiveTrue();

    List<Company> findAllByUserId(Long userId);

    long countByIsActiveTrue();
}
