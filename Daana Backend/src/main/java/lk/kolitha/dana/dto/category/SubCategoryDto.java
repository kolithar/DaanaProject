package lk.kolitha.dana.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubCategoryDto {
    private Long id;
    private String name;
    private String description;
    private String status;
}
