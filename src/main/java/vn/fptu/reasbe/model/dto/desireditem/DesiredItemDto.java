package vn.fptu.reasbe.model.dto.desireditem;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.fptu.reasbe.model.enums.item.ConditionItem;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DesiredItemDto {

    Integer categoryId;

    ConditionItem conditionItem;

    Integer brandId;

    BigDecimal minPrice;

    BigDecimal maxPrice;

    @NotBlank(message = "Description must not be blank")
    String description;
}
