package miju.com.robodelivery.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class APIError {
    private String statusMessage;
    private String errorCode;
    private String timestamp;
    private String path;
    private String method;
}
