package miju.com.robodelivery.dto.responses;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import miju.com.robodelivery.entities.Item;
import java.math.BigDecimal;


@Getter
@Setter
@Builder
public class ItemResponse {
    private String name;
    private BigDecimal weight;
    private String code;

    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .name(item.getName())
                .weight(item.getWeight())
                .code(item.getCode()).build();

    }

}
