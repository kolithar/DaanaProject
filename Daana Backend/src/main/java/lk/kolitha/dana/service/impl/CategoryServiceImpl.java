package lk.kolitha.dana.service.impl;

import lk.kolitha.dana.dto.category.CategoryWithSubCategoriesDto;
import lk.kolitha.dana.dto.category.SubCategoryDto;
import lk.kolitha.dana.entity.Category;
import lk.kolitha.dana.entity.SubCategory;
import lk.kolitha.dana.enums.Status;
import lk.kolitha.dana.repository.CategoryRepository;
import lk.kolitha.dana.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Override
    public List<CategoryWithSubCategoriesDto> getAllCategoriesWithSubCategories() {
        log.info("Fetching all categories with subcategories");
        List<Category> categories = categoryRepository.findAllActiveCategoriesWithSubCategories(Status.ACTIVE, Status.ACTIVE);
        
        return categories.stream()
                .map(this::convertToCategoryWithSubCategoriesDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Category entity to CategoryWithSubCategoriesDto
     * @param category Category entity
     * @return CategoryWithSubCategoriesDto
     */
    private CategoryWithSubCategoriesDto convertToCategoryWithSubCategoriesDto(Category category) {
        CategoryWithSubCategoriesDto dto = new CategoryWithSubCategoriesDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setStatus(category.getStatus().toString());
        
        // Convert subcategories
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            List<SubCategoryDto> subCategoryDtos = category.getSubCategories().stream()
                    .filter(subCategory -> subCategory.getStatus() == Status.ACTIVE)
                    .map(this::convertToSubCategoryDto)
                    .collect(Collectors.toList());
            dto.setSubCategories(subCategoryDtos);
        }
        
        return dto;
    }
    
    /**
     * Convert SubCategory entity to SubCategoryDto
     * @param subCategory SubCategory entity
     * @return SubCategoryDto
     */
    private SubCategoryDto convertToSubCategoryDto(SubCategory subCategory) {
        SubCategoryDto dto = new SubCategoryDto();
        dto.setId(subCategory.getId());
        dto.setName(subCategory.getName());
        dto.setDescription(subCategory.getDescription());
        dto.setStatus(subCategory.getStatus().toString());
        return dto;
    }
}
