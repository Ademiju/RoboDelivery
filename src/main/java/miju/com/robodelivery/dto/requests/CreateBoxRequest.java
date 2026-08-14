package miju.com.robodelivery.dto.requests;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBoxRequest{

    @NotBlank @Size(max=20)
    private String txref;
    @NotNull
    private Double weightLimit;
    @Min(0)
    @Max(100)
    @NotNull
    private Integer batteryCapacity;

}
