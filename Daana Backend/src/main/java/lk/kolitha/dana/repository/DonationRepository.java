package lk.kolitha.dana.repository;

import lk.kolitha.dana.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    
    // Get total donation count for a charity
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.campaigns.charity.id = :charityId AND d.status = 'ACTIVE'")
    Long countDonationsByCharityId(@Param("charityId") Long charityId);
    
    // Get total donation amount for a charity
    @Query("SELECT COALESCE(SUM(d.netDonationAmount), 0) FROM Donation d WHERE d.campaigns.charity.id = :charityId AND d.status = 'ACTIVE'")
    BigDecimal getTotalDonationAmountByCharityId(@Param("charityId") Long charityId);
    
    // Get donation count for a specific campaign
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.campaigns.id = :campaignId AND d.status = 'ACTIVE'")
    Long countDonationsByCampaignId(@Param("campaignId") Long campaignId);
    
    // Get donations for a charity within date range
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.campaigns.charity.id = :charityId AND d.status = 'ACTIVE' AND d.created >= :startDate AND d.created <= :endDate")
    Long countDonationsByCharityIdAndDateRange(@Param("charityId") Long charityId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Get donation amount for a charity within date range
    @Query("SELECT COALESCE(SUM(d.netDonationAmount), 0) FROM Donation d WHERE d.campaigns.charity.id = :charityId AND d.status = 'ACTIVE' AND d.created >= :startDate AND d.created <= :endDate")
    BigDecimal getTotalDonationAmountByCharityIdAndDateRange(@Param("charityId") Long charityId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Get paginated donations for a charity with filtering
    @Query("SELECT d FROM Donation d " +
           "JOIN FETCH d.campaigns c " +
           "LEFT JOIN FETCH d.registeredDonor rd " +
           "WHERE c.charity.id = :charityId " +
           "AND (:startDate IS NULL OR d.created >= :startDate) " +
           "AND (:endDate IS NULL OR d.created <= :endDate) " +
           "AND (:donorEmail IS NULL OR rd.email = :donorEmail) " +
           "AND (:isAnonymous IS NULL OR d.isAnonymousDonation = :isAnonymous) " +
           "AND (:programId IS NULL OR c.id = :programId) " +
           "ORDER BY d.created DESC")
    Page<Donation> findDonationsByCharityIdWithFilters(
            @Param("charityId") Long charityId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("donorEmail") String donorEmail,
            @Param("isAnonymous") Boolean isAnonymous,
            @Param("programId") Long programId,
            Pageable pageable);
    
    @Query("SELECT d FROM Donation d " +
           "JOIN FETCH d.campaigns c " +
           "LEFT JOIN FETCH c.charity ch " +
           "LEFT JOIN FETCH c.subCategory sc " +
           "LEFT JOIN FETCH sc.category cat " +
           "WHERE d.registeredDonor.id = :donorId " +
           "AND (:startDate IS NULL OR d.created >= :startDate) " +
           "AND (:endDate IS NULL OR d.created <= :endDate) " +
           "ORDER BY d.created DESC")
    Page<Donation> findDonationsByDonorIdWithFilters(
            @Param("donorId") Long donorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable);
}
