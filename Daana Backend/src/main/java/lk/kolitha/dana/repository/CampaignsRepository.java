package lk.kolitha.dana.repository;

import lk.kolitha.dana.entity.Campaigns;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignsRepository extends JpaRepository<Campaigns, Long> {


    @Query("SELECT p FROM Campaigns p LEFT JOIN FETCH p.subCategory WHERE p.deleted = false ORDER BY p.raised DESC")
    List<Campaigns> findTrendingProgramsWithSubCategory(Pageable pageable);

    @Query("SELECT p FROM Campaigns p LEFT JOIN FETCH p.subCategory WHERE p.deleted = false ORDER BY p.created DESC")
    List<Campaigns> findLatestProgramsWithSubCategory(Pageable pageable);

    @Query("SELECT new lk.kolitha.dana.dto.program.BasicProgramCardDataResDto(p.id, p.programName, p.title, p.description, sc.name, p.programLocation, p.programImage, p.urlName, p.targetDonationAmount, p.raised) FROM Campaigns p " +
            "INNER JOIN SubCategory sc ON p.subCategory.id = sc.id " +
            "INNER JOIN Category c ON sc.category.id = c.id " +
            "WHERE p.deleted = false " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:subCategoryId IS NULL OR sc.id = :subCategoryId) " +
            "AND (:searchText IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchText, '%')) "+
            "OR LOWER(p.programName) LIKE LOWER(CONCAT('%', :searchText, '%'))) "+
            "ORDER BY p.created DESC")
    Page<lk.kolitha.dana.dto.program.BasicProgramCardDataResDto> filterProgram(Long categoryId,
                                                                               Long subCategoryId,
                                                                               String searchText,
                                                                               Pageable pageable);


    @Query("SELECT p FROM Campaigns p WHERE p.deleted = false AND p.urlName = :urlName")
    Optional<Campaigns> findByUrlNameWithRelations(@Param("urlName") String urlName);

    @Query("SELECT p FROM Campaigns p LEFT JOIN FETCH p.subCategory WHERE p.charity.id = :charityId AND p.deleted = false")
    Page<Campaigns> findByCharityIdAndDeletedFalse(@Param("charityId") Long charityId, Pageable pageable);

    @Query("SELECT p FROM Campaigns p LEFT JOIN FETCH p.subCategory LEFT JOIN FETCH p.charity WHERE p.id = :programId AND p.charity.id = :charityId AND p.deleted = false")
    Optional<Campaigns> findByIdAndCharityIdAndDeletedFalse(@Param("programId") Long programId, @Param("charityId") Long charityId);

    @Query("SELECT COUNT(d) > 0 FROM Donation d WHERE d.campaigns.id = :campaignId AND d.status = 'ACTIVE'")
    boolean hasActiveDonations(@Param("campaignId") Long campaignId);

    }



