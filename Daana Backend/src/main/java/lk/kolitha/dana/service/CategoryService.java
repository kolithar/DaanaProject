package lk.kolitha.dana.service;

import lk.kolitha.dana.dto.category.CategoryWithSubCategoriesDto;

import java.util.List;

public interface CategoryService {
    

    List<CategoryWithSubCategoriesDto> getAllCategoriesWithSubCategories();
}
