package miju.com.robodelivery.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatteryCapacity {
    private int capacity;
    private String boxTxref;
}
