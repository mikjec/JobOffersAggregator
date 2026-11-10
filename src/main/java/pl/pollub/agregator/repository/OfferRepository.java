package pl.pollub.agregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.pollub.agregator.entity.Offer;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findAllByIsActiveTrue();

    Optional<Offer> findByIdAndIsActiveTrue(Long id);

    List<Offer> findAllByCompanyId(Long companyId);

    List<Offer> findAllByCompanyIdAndIsActiveTrue(Long companyId);
}
