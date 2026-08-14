package miju.com.robodelivery.dto.requests;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemRequest {
    @NotBlank
    @Pattern(regexp="[A-Za-z0-9_-]+", message="must contain only letters, numbers, hyphens, or underscores")
    private String name;
    @NotNull
    @DecimalMin(value="0.01")
    private BigDecimal weight;
    @NotBlank
    @Pattern(regexp="[A-Z0-9_]+", message="must contain only uppercase letters, numbers, or underscores")
    private String code;


}
