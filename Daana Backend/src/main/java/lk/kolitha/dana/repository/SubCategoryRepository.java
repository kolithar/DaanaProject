package lk.kolitha.dana.repository;

import lk.kolitha.dana.entity.Category;
import lk.kolitha.dana.entity.SubCategory;
import lk.kolitha.dana.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    Optional<SubCategory> findById( Long id );

}
