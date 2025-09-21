package lk.kolitha.dana.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryWithSubCategoriesDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String status;
    private List<SubCategoryDto> subCategories;
}
