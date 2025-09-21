package lk.kolitha.dana.service;

import lk.kolitha.dana.dto.program.AdminFullProgramDto;
import lk.kolitha.dana.dto.program.BasicProgramCardDataResDto;
import lk.kolitha.dana.dto.program.CampaignUpdateRequestDto;
import lk.kolitha.dana.dto.program.CampaignUpdateStep1Dto;
import lk.kolitha.dana.dto.program.CampaignUpdateStep2Dto;
import lk.kolitha.dana.dto.program.CharityDashboardStatsDto;
import lk.kolitha.dana.dto.program.CharityProgramTableDto;
import lk.kolitha.dana.dto.program.FullProgramDto;
import lk.kolitha.dana.dto.program.ProgramRegisterRequestDto;
import lk.kolitha.dana.dto.program.ProgramRegisterStep1Dto;
import lk.kolitha.dana.dto.program.ProgramRegisterStep2Dto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProgramService {


    List<BasicProgramCardDataResDto> getTrendingPrograms();

    List<BasicProgramCardDataResDto> getLatestPrograms();

    Page<BasicProgramCardDataResDto> filterPrograms(Long categoryId,
                                     Long subCategoryId,
                                     String searchText,
                                     Pageable pageable);

    FullProgramDto getProgramByUrl(String urlName);

    // New 2-step registration methods
    AdminFullProgramDto registerProgramStep1(Long charityId, ProgramRegisterStep1Dto step1Dto);
    AdminFullProgramDto registerProgramStep2(Long charityId, ProgramRegisterStep2Dto step2Dto);
    
    // New 2-step update methods
    AdminFullProgramDto updateCampaignStep1(Long campaignId, Long charityId, CampaignUpdateStep1Dto step1Dto);
    AdminFullProgramDto updateCampaignStep2(Long campaignId, Long charityId, CampaignUpdateStep2Dto step2Dto);
    
    // Legacy methods - keeping for backward compatibility
    AdminFullProgramDto addNewCampaign(Long charityId, ProgramRegisterRequestDto requestDto);
    AdminFullProgramDto updateCampaign(Long campaignId, Long charityId, CampaignUpdateRequestDto updateRequest);
    
    Page<CharityProgramTableDto> getCharityPrograms(Long charityId, Pageable pageable);
    AdminFullProgramDto getCharityProgramById(Long programId, Long charityId);
    void deleteCampaign(Long campaignId, Long charityId);
    
    // Dashboard Statistics
    CharityDashboardStatsDto getCharityDashboardStats(Long charityId);

}


