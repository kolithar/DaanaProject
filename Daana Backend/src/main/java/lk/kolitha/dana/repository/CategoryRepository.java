package lk.kolitha.dana.repository;

import lk.kolitha.dana.entity.Category;
import lk.kolitha.dana.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories sc WHERE c.status = :categoryStatus AND (sc.status = :subCategoryStatus OR sc.status IS NULL) ORDER BY c.name")
    List<Category> findAllActiveCategoriesWithSubCategories(@org.springframework.data.repository.query.Param("categoryStatus") Status categoryStatus, 
                                                           @org.springframework.data.repository.query.Param("subCategoryStatus") Status subCategoryStatus);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories ORDER BY c.name")
    List<Category> findAllCategoriesWithSubCategories();
}
