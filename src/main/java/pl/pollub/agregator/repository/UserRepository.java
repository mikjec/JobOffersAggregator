package pl.pollub.agregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.pollub.agregator.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
