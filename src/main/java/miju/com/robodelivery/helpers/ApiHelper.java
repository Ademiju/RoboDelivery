package miju.com.robodelivery.helpers;

import lombok.RequiredArgsConstructor;
import miju.com.robodelivery.enums.ResponseCode;
import miju.com.robodelivery.exceptions.APIErrorException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static miju.com.robodelivery.enums.ResponseCode.INVALID_KEY;

@RequiredArgsConstructor
@Service
public class ApiHelper {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SystemProperties systemProperties;

    public APIErrorException getException(ResponseCode responseCode) {
        return APIErrorException.builder()
                .statusCode(responseCode.getCode())
                .httpStatus(responseCode.getHttpStatus())
                .message(responseCode.getDescription())
                .timestamp(getTimestamp())
                .build();
    }

    public String getTimestamp() {
        return dateFormat.format(new Date());
    }

    public void validateReturnHeader(String auth) {
        if(!StringUtils.hasText(auth) || !auth.equals(systemProperties.getSuccessfulReturnWebhookKey())) {
            throw getException(INVALID_KEY);
        }
    }
}
