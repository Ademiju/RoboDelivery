package miju.com.robodelivery.dto.requests;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
@Data
public class LoadItemsRequest {
    private String txref;
    private List<@Valid ItemRequest> items;
    private List<String> codes;

}
