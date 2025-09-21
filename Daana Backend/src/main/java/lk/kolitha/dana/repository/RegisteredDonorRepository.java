package lk.kolitha.dana.repository;

import lk.kolitha.dana.entity.RegisteredDonor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisteredDonorRepository extends JpaRepository<RegisteredDonor,Long> {
    Optional<RegisteredDonor> findByEmail(String email);

    Optional<RegisteredDonor> findFirstByEmailAndAccountVerifyStatus(String email, boolean accountVerifyStatus);

}
