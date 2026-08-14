package miju.com.robodelivery.dto.responses;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import miju.com.robodelivery.entities.Box;
import miju.com.robodelivery.enums.BoxState;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class BoxResponse {
    private String txref;
    private BigDecimal weightLimit;
    private BigDecimal loadedWeight;
    private BigDecimal remainingWeight;
    private int batteryCapacity;
    private BoxState state; List<ItemResponse> items;

 public static BoxResponse from(Box box) {
  return BoxResponse.builder()
          .txref(box.getTxref())
          .weightLimit(box.getWeightLimit())
          .loadedWeight(box.loadedWeight())
          .remainingWeight(box.getWeightLimit().subtract(box.loadedWeight()))
          .batteryCapacity(box.getBatteryCapacity())
          .state(box.getState())
          .items(box.getItems().stream().map(ItemResponse::from).toList()).build();
 }
}
