package lk.kolitha.dana.service.impl;

import jakarta.transaction.Transactional;
import lk.kolitha.dana.constants.ApplicationConstants;
import lk.kolitha.dana.dto.program.*;
import lk.kolitha.dana.dto.CharityDto;
import lk.kolitha.dana.dto.category.SubCategoryDto;
import lk.kolitha.dana.dto.category.CategoryDto;
import lk.kolitha.dana.entity.Campaigns;
import lk.kolitha.dana.entity.Charity;
import lk.kolitha.dana.entity.SubCategory;
import lk.kolitha.dana.entity.Category;
import lk.kolitha.dana.enums.Status;
import lk.kolitha.dana.exception.CustomServiceException;
import lk.kolitha.dana.repository.CharityRepository;
import lk.kolitha.dana.repository.CampaignsRepository;
import lk.kolitha.dana.repository.DonationRepository;
import lk.kolitha.dana.repository.SubCategoryRepository;
import lk.kolitha.dana.service.ProgramService;
import lk.kolitha.dana.util.AwsFileHandler;
import lk.kolitha.dana.util.UrlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProgramServiceImpl implements ProgramService {
    
    private final CampaignsRepository campaignsRepository;
    private final AwsFileHandler awsFileHandler;
    // private final CategoryRepository categoryRepository; // Unused in current implementation
    private final CharityRepository charityRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final DonationRepository donationRepository;
    private final ModelMapper modelMapper;


    @Override
    public List<BasicProgramCardDataResDto> getTrendingPrograms() {
        log.info("Fetching trending programs");
        Pageable pageable = PageRequest.of(0, 10);
        List<Campaigns> campaigns = campaignsRepository.findTrendingProgramsWithSubCategory(pageable);
        
        return campaigns.stream()
                .map(this::convertToBasicProgramCardDataResDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BasicProgramCardDataResDto> getLatestPrograms() {
        log.info("Fetching latest programs");
        Pageable pageable = PageRequest.of(0, 10);
        List<Campaigns> campaigns = campaignsRepository.findLatestProgramsWithSubCategory(pageable);
        
        return campaigns.stream()
                .map(this::convertToBasicProgramCardDataResDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Program entity to BasicProgramCardDataResDto
     * @param campaigns Program entity
     * @return BasicProgramCardDataResDto
     */
    private BasicProgramCardDataResDto convertToBasicProgramCardDataResDto(Campaigns campaigns) {
        BasicProgramCardDataResDto dto = new BasicProgramCardDataResDto();
        dto.setProgramId(campaigns.getId());
        dto.setProgramName(campaigns.getProgramName());
        dto.setProgramTitle(campaigns.getTitle());
        dto.setProgramDescription(campaigns.getDescription());
        dto.setLocation(campaigns.getProgramLocation());
        dto.setProgramImageUrl(campaigns.getProgramImage());
        dto.setTargetDonationAmount(campaigns.getTargetDonationAmount());
        dto.setRaised(campaigns.getRaised());
        dto.setUrlSlug(campaigns.getUrlName());
        
        // Set subcategory name if available
        if (campaigns.getSubCategory() != null) {
            dto.setSubCategoryName(campaigns.getSubCategory().getName());
        }
        
        return dto;
    }

    @Override
    public Page<BasicProgramCardDataResDto> filterPrograms(Long categoryId,
                                        Long subCategoryId,
                                        String searchText,
                                        Pageable pageable) {
        log.info("Fetching filtered programs for categoryId: {}, subCategoryId: {}, searchText: {}", categoryId, subCategoryId, searchText);
        return campaignsRepository.filterProgram(categoryId, subCategoryId, searchText, pageable);
    }


    @Override
    public FullProgramDto getProgramByUrl(String urlName) {
        log.info("Fetching full program data for urlName: {}", urlName);
        
        Campaigns campaigns = campaignsRepository.findByUrlNameWithRelations(urlName)
                .orElseThrow(() -> new CustomServiceException("Program not found with urlName: " + urlName));
        
        return convertToFullProgramDto(campaigns);
    }

    @Override
    @Transactional
    public AdminFullProgramDto registerProgramStep1(Long charityId, ProgramRegisterStep1Dto step1Dto) {
        log.info("Starting program registration step 1 for charity ID: {}", charityId);
        
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));
        SubCategory subCategory = subCategoryRepository.findById(step1Dto.getSubCategoryId())
                .orElseThrow(() -> new CustomServiceException("SubCategory not found with id: " + step1Dto.getSubCategoryId()));

        Campaigns campaigns = new Campaigns();
        campaigns.setProgramName(step1Dto.getProgramName());
        campaigns.setCharity(charity);
        campaigns.setSubCategory(subCategory);
        campaigns.setProgramLocation(step1Dto.getProgramLocation());
        campaigns.setStatus(Status.DRAFT);
        campaigns.setTargetDonationAmount(step1Dto.getTargetDonationAmount());
        campaigns.setRaised(step1Dto.getRaised() != null ? step1Dto.getRaised() : BigDecimal.ZERO);
        campaigns.setContactPersonMobile(step1Dto.getContactPersonMobile());
        campaigns.setContactPersonEmail(step1Dto.getContactPersonEmail());
        campaigns.setDescription(step1Dto.getDescription());
        campaigns.setTitle(step1Dto.getTitle());
        campaigns.setStartDate(step1Dto.getStartDate());
        campaigns.setEndDate(step1Dto.getEndDate());
        campaigns.setContactPersonName(step1Dto.getContactPersonName());

        String url = UrlGenerator.generate(step1Dto.getProgramName(), 25);
        campaigns.setUrlName(url);

        Campaigns savedCampaigns = campaignsRepository.save(campaigns);
        log.info("Program step 1 completed successfully. Program ID: {}", savedCampaigns.getId());
        
        return modelMapper.map(savedCampaigns, AdminFullProgramDto.class);
    }

    @Override
    @Transactional
    public AdminFullProgramDto registerProgramStep2(Long charityId, ProgramRegisterStep2Dto step2Dto) {
        log.info("Starting program registration step 2 for program ID: {}", step2Dto.getProgramId());
        
        // Find the program and verify ownership
        Campaigns campaign = campaignsRepository.findByIdAndCharityIdAndDeletedFalse(step2Dto.getProgramId(), charityId)
                .orElseThrow(() -> new CustomServiceException("Program not found with id: " + step2Dto.getProgramId() + " for charity: " + charityId));

        // Handle file uploads
        handleFileUploadsForStep2(campaign, step2Dto);
        
        // Update status to PENDING for admin review
        campaign.setStatus(Status.PENDING);
        campaign.setUpdated(new java.util.Date());
        
        Campaigns savedCampaign = campaignsRepository.save(campaign);
        log.info("Program step 2 completed successfully. Program ID: {}", savedCampaign.getId());
        
        return modelMapper.map(savedCampaign, AdminFullProgramDto.class);
    }

    @Override
    @Transactional
    public AdminFullProgramDto addNewCampaign(Long charityId, ProgramRegisterRequestDto requestDto) {

        Charity charity = charityRepository.findById(charityId).orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));
        SubCategory category = subCategoryRepository.findById(requestDto.getSubCategoryId()).orElseThrow(() -> new CustomServiceException("SubCategory not found with id: " + requestDto.getSubCategoryId()));

        Campaigns campaigns = new Campaigns();
        campaigns.setProgramName(requestDto.getProgramName());
        campaigns.setCharity(charity);
        campaigns.setSubCategory(category);
        campaigns.setProgramLocation(requestDto.getProgramLocation());
        campaigns.setStatus(Status.DRAFT);
        campaigns.setTargetDonationAmount(requestDto.getTargetDonationAmount());
        campaigns.setRaised(requestDto.getRaised());
        campaigns.setProgramLocation(requestDto.getProgramLocation());
        campaigns.setContactPersonMobile(requestDto.getContactPersonMobile());
        campaigns.setContactPersonEmail(requestDto.getContactPersonEmail());
        campaigns.setDescription(requestDto.getDescription());
        campaigns.setTitle(requestDto.getTitle());
        campaigns.setStartDate(requestDto.getStartDate());
        campaigns.setEndDate(requestDto.getEndDate());

        String url = UrlGenerator.generate(requestDto.getProgramName(),25 );
        campaigns.setUrlName(url);

        // Handle program image upload
        if (requestDto.getProgramImage() != null && !requestDto.getProgramImage().isEmpty()) {
            String logoFileName = requestDto.getProgramName() + "_cover_" + System.currentTimeMillis();
            Optional<String> logoFileUrl = awsFileHandler.uploadToS3Bucket(
                    requestDto.getProgramImage(),
                    logoFileName,
                    ApplicationConstants.S3FolderConstants.LOGO_IMAGES
            );

            if (logoFileUrl.isPresent()) {
                campaigns.setProgramImage(logoFileUrl.get());
            } else {
                log.warn("Failed to cover image upload logo to S3 for program ID: {}", campaigns.getId());
            }
        }

        // Handle program video upload
        if (requestDto.getProgramVideo() != null && !requestDto.getProgramVideo().isEmpty()) {
            String videoFileName = requestDto.getProgramName() + "_video_" + System.currentTimeMillis();

            Optional<String> videoUrl = awsFileHandler.uploadToS3Bucket(
                    requestDto.getProgramVideo(),
                    videoFileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_VIDEO
            );

            if (videoUrl.isPresent()) {
                campaigns.setProgramVideo(videoUrl.get());
            } else {
                log.warn("Failed to upload video to S3 for program ID: {}, continuing without video", campaigns.getId());
            }
        }

        // Handle related document 1 upload
        if (requestDto.getRelatedDocument1() != null && !requestDto.getRelatedDocument1().isEmpty()) {
            String document1FileName = requestDto.getProgramName() + "_document1_" + System.currentTimeMillis();
            Optional<String> document1Url = awsFileHandler.uploadToS3Bucket(
                    requestDto.getRelatedDocument1(),
                    document1FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document1Url.isPresent()) {
                campaigns.setRelatedDocument1(document1Url.get());
            } else {
                log.warn("Failed to upload related document 1 to S3 for program ID: {}", campaigns.getId());
            }
        }

        // Handle related document 2 upload
        if (requestDto.getRelatedDocument2() != null && !requestDto.getRelatedDocument2().isEmpty()) {
            String document2FileName = requestDto.getProgramName() + "_document2_" + System.currentTimeMillis();
            Optional<String> document2Url = awsFileHandler.uploadToS3Bucket(
                    requestDto.getRelatedDocument2(),
                    document2FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document2Url.isPresent()) {
                campaigns.setRelatedDocument2(document2Url.get());
            } else {
                log.warn("Failed to upload related document 2 to S3 for program ID: {}", campaigns.getId());
            }
        }

        // Handle related document 3 upload
        if (requestDto.getRelatedDocument3() != null && !requestDto.getRelatedDocument3().isEmpty()) {
            String document3FileName = requestDto.getProgramName() + "_document3_" + System.currentTimeMillis();
            Optional<String> document3Url = awsFileHandler.uploadToS3Bucket(
                    requestDto.getRelatedDocument3(),
                    document3FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document3Url.isPresent()) {
                campaigns.setRelatedDocument3(document3Url.get());
            } else {
                log.warn("Failed to upload related document 3 to S3 for program ID: {}", campaigns.getId());
            }
        }

        Campaigns savedCampaigns = campaignsRepository.save(campaigns);
        return modelMapper.map(savedCampaigns, AdminFullProgramDto.class);
    }

    /**
     * Convert Program entity to FullProgramDto with all relationships
     * @param campaigns Program entity
     * @return FullProgramDto
     */
    private FullProgramDto convertToFullProgramDto(Campaigns campaigns) {
        FullProgramDto dto = new FullProgramDto();
        
        // Map basic program fields
        dto.setUrlName(campaigns.getUrlName());
        dto.setProgramName(campaigns.getProgramName());
        dto.setTitle(campaigns.getTitle());
        dto.setDescription(campaigns.getDescription());
        dto.setContactPersonEmail(campaigns.getContactPersonEmail());
        dto.setContactPersonMobile(campaigns.getContactPersonMobile());
        dto.setContactPersonName(campaigns.getContactPersonName());
        dto.setProgramLocation(campaigns.getProgramLocation());
        dto.setTargetDonationAmount(campaigns.getTargetDonationAmount());
        dto.setRaised(campaigns.getRaised());
        dto.setProgramImage(campaigns.getProgramImage());
        dto.setProgramVideo(campaigns.getProgramVideo());
        dto.setStartDate(campaigns.getStartDate());
        dto.setEndDate(campaigns.getEndDate());
        dto.setCreated(campaigns.getCreated());
        dto.setUpdated(campaigns.getUpdated());
        
        // Map charity information
        if (campaigns.getCharity() != null) {
            dto.setCharity(convertToCharityDto(campaigns.getCharity()));
        }
        
        // Map subcategory information
        if (campaigns.getSubCategory() != null) {
            dto.setSubCategory(convertToSubCategoryDto(campaigns.getSubCategory()));
            
            // Map category information from subcategory
            if (campaigns.getSubCategory().getCategory() != null) {
                dto.setCategory(convertToCategoryDto(campaigns.getSubCategory().getCategory()));
            }
        }
        
        return dto;
    }
    
    /**
     * Convert Charity entity to CharityDto
     */
    private CharityDto convertToCharityDto(Charity charity) {
        CharityDto dto = new CharityDto();
        dto.setName(charity.getName());
        dto.setEmail(charity.getEmail());
        dto.setCharityDescription(charity.getDescription());
        dto.setExecutionType(charity.getExecutionType());
        dto.setCharityLogo(charity.getLogoUrl());
        dto.setCharityRepresentPerson(charity.getContactPersonName());
        dto.setNicNumber(charity.getNicNumberOrRegistrationNumber());
        dto.setContactPersonName(charity.getContactPersonName());
        dto.setContactPersonMobile(charity.getContactPersonMobile());
        dto.setCreated(charity.getCreated());
        dto.setUpdated(charity.getUpdated());
        return dto;
    }
    
    /**
     * Convert SubCategory entity to SubCategoryDto
     */
    private SubCategoryDto convertToSubCategoryDto(SubCategory subCategory) {
        SubCategoryDto dto = new SubCategoryDto();
        dto.setId(subCategory.getId());
        dto.setName(subCategory.getName());
        dto.setDescription(subCategory.getDescription());
        dto.setStatus(subCategory.getStatus().toString());
        return dto;
    }
    
    /**
     * Convert Category entity to CategoryDto
     */
    private CategoryDto convertToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setStatus(category.getStatus().toString());
        return dto;
    }

    @Override
    public Page<CharityProgramTableDto> getCharityPrograms(Long charityId, Pageable pageable) {
        log.info("Fetching programs for charity ID: {}", charityId);
        
        // Verify charity exists
        charityRepository.findById(charityId)
                .orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));
        
        Page<Campaigns> programs = campaignsRepository.findByCharityIdAndDeletedFalse(charityId, pageable);
        
        return programs.map(this::convertToCharityProgramTableDto);
    }

    @Override
    public AdminFullProgramDto getCharityProgramById(Long programId, Long charityId) {
        log.info("Fetching program ID: {} for charity ID: {}", programId, charityId);
        
        Campaigns campaigns = campaignsRepository.findByIdAndCharityIdAndDeletedFalse(programId, charityId)
                .orElseThrow(() -> new CustomServiceException("Program not found with id: " + programId + " for charity: " + charityId));
        
        return modelMapper.map(campaigns, AdminFullProgramDto.class);
    }

    /**
     * Convert Program entity to CharityProgramTableDto
     */
    private CharityProgramTableDto convertToCharityProgramTableDto(Campaigns campaigns) {
        CharityProgramTableDto dto = new CharityProgramTableDto();
        dto.setId(campaigns.getId());
        dto.setProgramName(campaigns.getProgramName());
        dto.setTitle(campaigns.getTitle());
        dto.setStatus(campaigns.getStatus().toString());
        dto.setTargetDonationAmount(campaigns.getTargetDonationAmount());
        dto.setRaised(campaigns.getRaised());
        dto.setCreated(campaigns.getCreated());
        dto.setUpdated(campaigns.getUpdated());
        dto.setProgramImage(campaigns.getProgramImage());
        
        // Set subcategory name if available
        if (campaigns.getSubCategory() != null) {
            dto.setSubCategoryName(campaigns.getSubCategory().getName());
        }
        
        return dto;
    }

    @Override
    @Transactional
    public AdminFullProgramDto updateCampaignStep1(Long campaignId, Long charityId, CampaignUpdateStep1Dto step1Dto) {
        log.info("Starting campaign update step 1 for campaign ID: {}", campaignId);
        
        // Find the campaign and verify ownership
        Campaigns campaign = campaignsRepository.findByIdAndCharityIdAndDeletedFalse(campaignId, charityId)
                .orElseThrow(() -> new CustomServiceException("Campaign not found with id: " + campaignId + " for charity: " + charityId));
        
        // Update basic fields
        campaign.setProgramName(step1Dto.getProgramName());
        campaign.setTitle(step1Dto.getTitle());
        campaign.setDescription(step1Dto.getDescription());
        campaign.setContactPersonEmail(step1Dto.getContactPersonEmail());
        campaign.setContactPersonMobile(step1Dto.getContactPersonMobile());
        campaign.setContactPersonName(step1Dto.getContactPersonName());
        campaign.setProgramLocation(step1Dto.getProgramLocation());
        campaign.setTargetDonationAmount(step1Dto.getTargetDonationAmount());
        campaign.setStartDate(step1Dto.getStartDate());
        campaign.setEndDate(step1Dto.getEndDate());
        
        // Update subcategory if provided
        SubCategory subCategory = subCategoryRepository.findById(step1Dto.getSubCategoryId())
                .orElseThrow(() -> new CustomServiceException("SubCategory not found with id: " + step1Dto.getSubCategoryId()));
        campaign.setSubCategory(subCategory);
        
        // Update timestamp
        campaign.setUpdated(new java.util.Date());
        
        Campaigns savedCampaign = campaignsRepository.save(campaign);
        log.info("Campaign update step 1 completed successfully. Campaign ID: {}", campaignId);
        
        return modelMapper.map(savedCampaign, AdminFullProgramDto.class);
    }

    @Override
    @Transactional
    public AdminFullProgramDto updateCampaignStep2(Long campaignId, Long charityId, CampaignUpdateStep2Dto step2Dto) {
        log.info("Starting campaign update step 2 for campaign ID: {}", campaignId);
        
        // Find the campaign and verify ownership
        Campaigns campaign = campaignsRepository.findByIdAndCharityIdAndDeletedFalse(campaignId, charityId)
                .orElseThrow(() -> new CustomServiceException("Campaign not found with id: " + campaignId + " for charity: " + charityId));
        
        // Handle file uploads
        handleFileUploadsForUpdateStep2(campaign, step2Dto);
        
        // Set status to PENDING when campaign is updated
        campaign.setStatus(Status.PENDING);
        campaign.setUpdated(new java.util.Date());
        
        Campaigns savedCampaign = campaignsRepository.save(campaign);
        log.info("Campaign update step 2 completed successfully. Campaign ID: {}", campaignId);
        
        return modelMapper.map(savedCampaign, AdminFullProgramDto.class);
    }

    @Override
    @Transactional
    public AdminFullProgramDto updateCampaign(Long campaignId, Long charityId, CampaignUpdateRequestDto updateRequest) {
        log.info("Updating campaign ID: {} for charity ID: {}", campaignId, charityId);
        
        // Find the campaign and verify ownership
        Campaigns campaign = campaignsRepository.findByIdAndCharityIdAndDeletedFalse(campaignId, charityId)
                .orElseThrow(() -> new CustomServiceException("Campaign not found with id: " + campaignId + " for charity: " + charityId));
        
        // Update basic fields
        if (updateRequest.getProgramName() != null) {
            campaign.setProgramName(updateRequest.getProgramName());
        }
        if (updateRequest.getTitle() != null) {
            campaign.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getDescription() != null) {
            campaign.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getContactPersonEmail() != null) {
            campaign.setContactPersonEmail(updateRequest.getContactPersonEmail());
        }
        if (updateRequest.getContactPersonMobile() != null) {
            campaign.setContactPersonMobile(updateRequest.getContactPersonMobile());
        }
        if (updateRequest.getContactPersonName() != null) {
            campaign.setContactPersonName(updateRequest.getContactPersonName());
        }
        if (updateRequest.getProgramLocation() != null) {
            campaign.setProgramLocation(updateRequest.getProgramLocation());
        }
        if (updateRequest.getTargetDonationAmount() != null) {
            campaign.setTargetDonationAmount(updateRequest.getTargetDonationAmount());
        }
        
        // Update subcategory if provided
        if (updateRequest.getSubCategoryId() != null) {
            SubCategory subCategory = subCategoryRepository.findById(updateRequest.getSubCategoryId())
                    .orElseThrow(() -> new CustomServiceException("SubCategory not found with id: " + updateRequest.getSubCategoryId()));
            campaign.setSubCategory(subCategory);
        }
        
        // Handle file uploads
        handleFileUploads(campaign, updateRequest);
        
        // Set status to PENDING when campaign is updated
        campaign.setStatus(Status.PENDING);
        campaign.setUpdated(new java.util.Date());
        
        Campaigns savedCampaign = campaignsRepository.save(campaign);
        log.info("Campaign updated successfully. Status changed to PENDING for campaign ID: {}", campaignId);
        
        return modelMapper.map(savedCampaign, AdminFullProgramDto.class);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long campaignId, Long charityId) {
        log.info("Attempting to delete campaign ID: {} for charity ID: {}", campaignId, charityId);
        
        // Find the campaign and verify ownership
        Campaigns campaign = campaignsRepository.findByIdAndCharityIdAndDeletedFalse(campaignId, charityId)
                .orElseThrow(() -> new CustomServiceException("Campaign not found with id: " + campaignId + " for charity: " + charityId));
        
        // Check if there are any active donations
        boolean hasActiveDonations = campaignsRepository.hasActiveDonations(campaignId);
        if (hasActiveDonations) {
            log.warn("Cannot delete campaign ID: {} - has active donations", campaignId);
            throw new CustomServiceException("Cannot delete campaign. There are active donations associated with this campaign.");
        }
        
        // Soft delete the campaign
        campaign.setDeleted(true);
        campaign.setUpdated(new java.util.Date());
        campaignsRepository.save(campaign);
        
        log.info("Campaign deleted successfully (soft delete) for campaign ID: {}", campaignId);
    }

    /**
     * Handle file uploads for program registration step 2
     */
    private void handleFileUploadsForStep2(Campaigns campaign, ProgramRegisterStep2Dto step2Dto) {
        // Handle program image upload
        if (step2Dto.getProgramImage() != null && !step2Dto.getProgramImage().isEmpty()) {
            String logoFileName = campaign.getProgramName() + "_cover_" + System.currentTimeMillis();
            Optional<String> logoFileUrl = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getProgramImage(),
                    logoFileName,
                    ApplicationConstants.S3FolderConstants.LOGO_IMAGES
            );

            if (logoFileUrl.isPresent()) {
                campaign.setProgramImage(logoFileUrl.get());
            } else {
                log.warn("Failed to upload program image to S3 for program ID: {}", campaign.getId());
            }
        }

        // Handle program video upload
        if (step2Dto.getProgramVideo() != null && !step2Dto.getProgramVideo().isEmpty()) {
            String videoFileName = campaign.getProgramName() + "_video_" + System.currentTimeMillis();
            Optional<String> videoUrl = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getProgramVideo(),
                    videoFileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_VIDEO
            );

            if (videoUrl.isPresent()) {
                campaign.setProgramVideo(videoUrl.get());
            } else {
                log.warn("Failed to upload video to S3 for program ID: {}", campaign.getId());
            }
        }

        // Handle related document uploads
        if (step2Dto.getRelatedDocument1() != null && !step2Dto.getRelatedDocument1().isEmpty()) {
            String document1FileName = campaign.getProgramName() + "_document1_" + System.currentTimeMillis();
            Optional<String> document1Url = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getRelatedDocument1(),
                    document1FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document1Url.isPresent()) {
                campaign.setRelatedDocument1(document1Url.get());
            } else {
                log.warn("Failed to upload related document 1 to S3 for program ID: {}", campaign.getId());
            }
        }

        if (step2Dto.getRelatedDocument2() != null && !step2Dto.getRelatedDocument2().isEmpty()) {
            String document2FileName = campaign.getProgramName() + "_document2_" + System.currentTimeMillis();
            Optional<String> document2Url = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getRelatedDocument2(),
                    document2FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document2Url.isPresent()) {
                campaign.setRelatedDocument2(document2Url.get());
            } else {
                log.warn("Failed to upload related document 2 to S3 for program ID: {}", campaign.getId());
            }
        }

        if (step2Dto.getRelatedDocument3() != null && !step2Dto.getRelatedDocument3().isEmpty()) {
            String document3FileName = campaign.getProgramName() + "_document3_" + System.currentTimeMillis();
            Optional<String> document3Url = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getRelatedDocument3(),
                    document3FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document3Url.isPresent()) {
                campaign.setRelatedDocument3(document3Url.get());
            } else {
                log.warn("Failed to upload related document 3 to S3 for program ID: {}", campaign.getId());
            }
        }
    }

    /**
     * Handle file uploads for campaign update step 2
     */
    private void handleFileUploadsForUpdateStep2(Campaigns campaign, CampaignUpdateStep2Dto step2Dto) {
        // Handle program image upload
        if (step2Dto.getProgramImage() != null && !step2Dto.getProgramImage().isEmpty()) {
            String logoFileName = campaign.getProgramName() + "_cover_" + System.currentTimeMillis();
            Optional<String> logoFileUrl = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getProgramImage(),
                    logoFileName,
                    ApplicationConstants.S3FolderConstants.LOGO_IMAGES
            );

            if (logoFileUrl.isPresent()) {
                campaign.setProgramImage(logoFileUrl.get());
            } else {
                log.warn("Failed to upload program image to S3 for campaign ID: {}", campaign.getId());
            }
        }

        // Handle program video upload
        if (step2Dto.getProgramVideo() != null && !step2Dto.getProgramVideo().isEmpty()) {
            String videoFileName = campaign.getProgramName() + "_video_" + System.currentTimeMillis();
            Optional<String> videoUrl = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getProgramVideo(),
                    videoFileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_VIDEO
            );

            if (videoUrl.isPresent()) {
                campaign.setProgramVideo(videoUrl.get());
            } else {
                log.warn("Failed to upload video to S3 for campaign ID: {}", campaign.getId());
            }
        }

        // Handle related document uploads
        if (step2Dto.getRelatedDocument1() != null && !step2Dto.getRelatedDocument1().isEmpty()) {
            String document1FileName = campaign.getProgramName() + "_document1_" + System.currentTimeMillis();
            Optional<String> document1Url = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getRelatedDocument1(),
                    document1FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document1Url.isPresent()) {
                campaign.setRelatedDocument1(document1Url.get());
            } else {
                log.warn("Failed to upload related document 1 to S3 for campaign ID: {}", campaign.getId());
            }
        }

        if (step2Dto.getRelatedDocument2() != null && !step2Dto.getRelatedDocument2().isEmpty()) {
            String document2FileName = campaign.getProgramName() + "_document2_" + System.currentTimeMillis();
            Optional<String> document2Url = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getRelatedDocument2(),
                    document2FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document2Url.isPresent()) {
                campaign.setRelatedDocument2(document2Url.get());
            } else {
                log.warn("Failed to upload related document 2 to S3 for campaign ID: {}", campaign.getId());
            }
        }

        if (step2Dto.getRelatedDocument3() != null && !step2Dto.getRelatedDocument3().isEmpty()) {
            String document3FileName = campaign.getProgramName() + "_document3_" + System.currentTimeMillis();
            Optional<String> document3Url = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getRelatedDocument3(),
                    document3FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document3Url.isPresent()) {
                campaign.setRelatedDocument3(document3Url.get());
            } else {
                log.warn("Failed to upload related document 3 to S3 for campaign ID: {}", campaign.getId());
            }
        }
    }

    /**
     * Handle file uploads for campaign update
     */
    private void handleFileUploads(Campaigns campaign, CampaignUpdateRequestDto updateRequest) {
        // Handle program image upload
        if (updateRequest.getProgramImage() != null && !updateRequest.getProgramImage().isEmpty()) {
            String logoFileName = campaign.getProgramName() + "_cover_" + System.currentTimeMillis();
            Optional<String> logoFileUrl = awsFileHandler.uploadToS3Bucket(
                    updateRequest.getProgramImage(),
                    logoFileName,
                    ApplicationConstants.S3FolderConstants.LOGO_IMAGES
            );

            if (logoFileUrl.isPresent()) {
                campaign.setProgramImage(logoFileUrl.get());
            } else {
                log.warn("Failed to upload program image to S3 for campaign ID: {}", campaign.getId());
            }
        }

        // Handle program video upload
        if (updateRequest.getProgramVideo() != null && !updateRequest.getProgramVideo().isEmpty()) {
            String videoFileName = campaign.getProgramName() + "_video_" + System.currentTimeMillis();
            Optional<String> videoUrl = awsFileHandler.uploadToS3Bucket(
                    updateRequest.getProgramVideo(),
                    videoFileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_VIDEO
            );

            if (videoUrl.isPresent()) {
                campaign.setProgramVideo(videoUrl.get());
            } else {
                log.warn("Failed to upload video to S3 for campaign ID: {}", campaign.getId());
            }
        }

        // Handle related document uploads
        if (updateRequest.getRelatedDocument1() != null && !updateRequest.getRelatedDocument1().isEmpty()) {
            String document1FileName = campaign.getProgramName() + "_document1_" + System.currentTimeMillis();
            Optional<String> document1Url = awsFileHandler.uploadToS3Bucket(
                    updateRequest.getRelatedDocument1(),
                    document1FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document1Url.isPresent()) {
                campaign.setRelatedDocument1(document1Url.get());
            } else {
                log.warn("Failed to upload related document 1 to S3 for campaign ID: {}", campaign.getId());
            }
        }

        if (updateRequest.getRelatedDocument2() != null && !updateRequest.getRelatedDocument2().isEmpty()) {
            String document2FileName = campaign.getProgramName() + "_document2_" + System.currentTimeMillis();
            Optional<String> document2Url = awsFileHandler.uploadToS3Bucket(
                    updateRequest.getRelatedDocument2(),
                    document2FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document2Url.isPresent()) {
                campaign.setRelatedDocument2(document2Url.get());
            } else {
                log.warn("Failed to upload related document 2 to S3 for campaign ID: {}", campaign.getId());
            }
        }

        if (updateRequest.getRelatedDocument3() != null && !updateRequest.getRelatedDocument3().isEmpty()) {
            String document3FileName = campaign.getProgramName() + "_document3_" + System.currentTimeMillis();
            Optional<String> document3Url = awsFileHandler.uploadToS3Bucket(
                    updateRequest.getRelatedDocument3(),
                    document3FileName,
                    ApplicationConstants.S3FolderConstants.PROGRAM_DOCUMENTS
            );

            if (document3Url.isPresent()) {
                campaign.setRelatedDocument3(document3Url.get());
            } else {
                log.warn("Failed to upload related document 3 to S3 for campaign ID: {}", campaign.getId());
            }
        }
    }

    @Override
    public CharityDashboardStatsDto getCharityDashboardStats(Long charityId) {
        log.info("Getting dashboard statistics for charity ID: {}", charityId);
        
        try {
            // Verify charity exists
            charityRepository.findById(charityId)
                    .orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));
            
            CharityDashboardStatsDto stats = new CharityDashboardStatsDto();
            
            // Get program statistics
            List<Campaigns> allPrograms = campaignsRepository.findByCharityIdAndDeletedFalse(charityId, Pageable.unpaged()).getContent();
            
            // Count programs by status
            long totalPrograms = allPrograms.size();
            long activePrograms = allPrograms.stream().filter(p -> p.getStatus() == Status.ACTIVE).count();
            long pendingPrograms = allPrograms.stream().filter(p -> p.getStatus() == Status.PENDING).count();
            long draftPrograms = allPrograms.stream().filter(p -> p.getStatus() == Status.DRAFT).count();
            long rejectedPrograms = allPrograms.stream().filter(p -> p.getStatus() == Status.INACTIVE).count();
            
            stats.setTotalPrograms(totalPrograms);
            stats.setActivePrograms(activePrograms);
            stats.setPendingPrograms(pendingPrograms);
            stats.setDraftPrograms(draftPrograms);
            stats.setRejectedPrograms(rejectedPrograms);
            
            // Calculate donation statistics using real donation data
            BigDecimal totalRaised = donationRepository.getTotalDonationAmountByCharityId(charityId);
            long totalDonations = donationRepository.countDonationsByCharityId(charityId);
            
            BigDecimal totalTargetAmount = allPrograms.stream()
                    .map(Campaigns::getTargetDonationAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate average donation amount
            BigDecimal averageDonationAmount = totalDonations > 0 ? 
                    totalRaised.divide(BigDecimal.valueOf(totalDonations), 2, java.math.RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
            
            stats.setTotalRaised(totalRaised);
            stats.setTotalTargetAmount(totalTargetAmount);
            stats.setTotalDonations(totalDonations);
            stats.setAverageDonationAmount(averageDonationAmount);
            
            // Get recent programs (last 5)
            List<CharityDashboardStatsDto.RecentProgramDto> recentPrograms = allPrograms.stream()
                    .filter(p -> p.getCreated() != null)
                    .sorted((p1, p2) -> p2.getCreated().compareTo(p1.getCreated()))
                    .limit(5)
                    .map(this::convertToRecentProgramDto)
                    .collect(Collectors.toList());
            
            stats.setRecentPrograms(recentPrograms);
            
            // Get top performing programs (by completion percentage)
            List<CharityDashboardStatsDto.TopPerformingProgramDto> topPerformingPrograms = allPrograms.stream()
                    .filter(p -> p.getTargetDonationAmount() != null && p.getTargetDonationAmount().compareTo(BigDecimal.ZERO) > 0)
                    .map(this::convertToTopPerformingProgramDto)
                    .sorted((p1, p2) -> p2.getCompletionPercentage().compareTo(p1.getCompletionPercentage()))
                    .limit(5)
                    .collect(Collectors.toList());
            
            stats.setTopPerformingPrograms(topPerformingPrograms);
            
            // Get monthly statistics (last 6 months)
            List<CharityDashboardStatsDto.MonthlyStatsDto> monthlyStats = getMonthlyStats(allPrograms, charityId);
            stats.setMonthlyStats(monthlyStats);
        
            log.info("Dashboard statistics calculated successfully for charity ID: {}", charityId);
            return stats;
        } catch (Exception e) {
            log.error("Error calculating dashboard statistics for charity ID: {}", charityId, e);
            throw new CustomServiceException("Failed to calculate dashboard statistics: " + e.getMessage());
        }
    }
    
    private CharityDashboardStatsDto.RecentProgramDto convertToRecentProgramDto(Campaigns campaign) {
        CharityDashboardStatsDto.RecentProgramDto dto = new CharityDashboardStatsDto.RecentProgramDto();
        dto.setId(campaign.getId());
        dto.setProgramName(campaign.getProgramName());
        dto.setTitle(campaign.getTitle());
        dto.setStatus(campaign.getStatus() != null ? campaign.getStatus().toString() : "UNKNOWN");
        dto.setRaised(campaign.getRaised());
        dto.setTargetAmount(campaign.getTargetDonationAmount());
        dto.setCreatedDate(campaign.getCreated() != null ? campaign.getCreated().toString() : "N/A");
        return dto;
    }
    
    private CharityDashboardStatsDto.TopPerformingProgramDto convertToTopPerformingProgramDto(Campaigns campaign) {
        CharityDashboardStatsDto.TopPerformingProgramDto dto = new CharityDashboardStatsDto.TopPerformingProgramDto();
        dto.setId(campaign.getId());
        dto.setProgramName(campaign.getProgramName());
        dto.setTitle(campaign.getTitle());
        dto.setRaised(campaign.getRaised() != null ? campaign.getRaised() : BigDecimal.ZERO);
        dto.setTargetAmount(campaign.getTargetDonationAmount());
        
        // Calculate completion percentage
        if (campaign.getTargetDonationAmount() != null && 
            campaign.getTargetDonationAmount().compareTo(BigDecimal.ZERO) > 0 &&
            campaign.getRaised() != null) {
            BigDecimal completionPercentage = campaign.getRaised()
                    .divide(campaign.getTargetDonationAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            dto.setCompletionPercentage(completionPercentage);
        } else {
            dto.setCompletionPercentage(BigDecimal.ZERO);
        }
        
        dto.setDonationCount(donationRepository.countDonationsByCampaignId(campaign.getId()));
        return dto;
    }
    
    private List<CharityDashboardStatsDto.MonthlyStatsDto> getMonthlyStats(List<Campaigns> programs, Long charityId) {
        // Group programs by month for the last 6 months
        Map<String, List<Campaigns>> programsByMonth = programs.stream()
                .filter(p -> p.getCreated() != null)
                .collect(Collectors.groupingBy(p -> {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(p.getCreated());
                    return cal.get(java.util.Calendar.YEAR) + "-" + 
                           String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1);
                }));
        
        // Generate last 6 months
        List<CharityDashboardStatsDto.MonthlyStatsDto> monthlyStats = new ArrayList<>();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        for (int i = 5; i >= 0; i--) {
            cal.setTime(new java.util.Date());
            cal.add(java.util.Calendar.MONTH, -i);
            
            String monthKey = cal.get(java.util.Calendar.YEAR) + "-" + 
                             String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1);
            
            List<Campaigns> monthPrograms = programsByMonth.getOrDefault(monthKey, new ArrayList<>());
            
            CharityDashboardStatsDto.MonthlyStatsDto monthlyStat = new CharityDashboardStatsDto.MonthlyStatsDto();
            monthlyStat.setMonth(String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1));
            monthlyStat.setYear(String.valueOf(cal.get(java.util.Calendar.YEAR)));
            monthlyStat.setProgramsCreated((long) monthPrograms.size());
            
            // Calculate month start and end dates
            java.util.Calendar monthStart = java.util.Calendar.getInstance();
            monthStart.setTime(cal.getTime());
            monthStart.set(java.util.Calendar.DAY_OF_MONTH, 1);
            monthStart.set(java.util.Calendar.HOUR_OF_DAY, 0);
            monthStart.set(java.util.Calendar.MINUTE, 0);
            monthStart.set(java.util.Calendar.SECOND, 0);
            monthStart.set(java.util.Calendar.MILLISECOND, 0);
            
            java.util.Calendar monthEnd = java.util.Calendar.getInstance();
            monthEnd.setTime(cal.getTime());
            monthEnd.set(java.util.Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
            monthEnd.set(java.util.Calendar.HOUR_OF_DAY, 23);
            monthEnd.set(java.util.Calendar.MINUTE, 59);
            monthEnd.set(java.util.Calendar.SECOND, 59);
            monthEnd.set(java.util.Calendar.MILLISECOND, 999);
            
            // Get real donation data for this month
            BigDecimal monthRaised = donationRepository.getTotalDonationAmountByCharityIdAndDateRange(
                    charityId, monthStart.getTime(), monthEnd.getTime());
            Long monthDonations = donationRepository.countDonationsByCharityIdAndDateRange(
                    charityId, monthStart.getTime(), monthEnd.getTime());
            
            monthlyStat.setAmountRaised(monthRaised);
            monthlyStat.setDonationsReceived(monthDonations);
            
            monthlyStats.add(monthlyStat);
        }
        
        return monthlyStats;
    }


}
