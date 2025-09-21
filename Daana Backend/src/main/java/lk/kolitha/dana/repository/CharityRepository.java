package lk.kolitha.dana.repository;

import lk.kolitha.dana.entity.Charity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharityRepository extends JpaRepository<Charity,Long> {
    Optional<Charity> findByEmail(String email);
    Optional<Charity> findById(Long id);
    Optional<Charity> findFirstByEmailAndAccountVerifyStatus(String email, boolean accountVerifyStatus);

}
