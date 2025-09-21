package lk.kolitha.dana.dto.program;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharityDashboardStatsDto {
    
    // Program Statistics
    private Long totalPrograms;
    private Long activePrograms;
    private Long pendingPrograms;
    private Long draftPrograms;
    private Long rejectedPrograms;
    
    // Donation Statistics
    private BigDecimal totalRaised;
    private BigDecimal totalTargetAmount;
    private Long totalDonations;
    private BigDecimal averageDonationAmount;
    
    // Recent Activity
    private List<RecentProgramDto> recentPrograms;
    private List<TopPerformingProgramDto> topPerformingPrograms;
    
    // Monthly Statistics (last 6 months)
    private List<MonthlyStatsDto> monthlyStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentProgramDto {
        private Long id;
        private String programName;
        private String title;
        private String status;
        private BigDecimal raised;
        private BigDecimal targetAmount;
        private String createdDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformingProgramDto {
        private Long id;
        private String programName;
        private String title;
        private BigDecimal raised;
        private BigDecimal targetAmount;
        private BigDecimal completionPercentage;
        private Long donationCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStatsDto {
        private String month;
        private String year;
        private Long programsCreated;
        private BigDecimal amountRaised;
        private Long donationsReceived;
    }
}
