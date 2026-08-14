package miju.com.robodelivery.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {
    OK("0000", "Success.", HttpStatus.OK),
    TXREF_ALREADY_EXISTS("0001", "txref already exists." , HttpStatus.CONFLICT ),
    LOADING_ERROR("0002", "Only IDLE or LOADING boxes can accept items" , HttpStatus.BAD_REQUEST ),
    BATTERY_LOW_ERROR("0003", "Battery must be at least 25% to load a box" , HttpStatus.BAD_REQUEST ),
    WEIGHT_LIMIT_EXCEEDED("0004", "Items exceed the box weight limit" , HttpStatus.BAD_REQUEST ),
    IDLE_STATE_ERROR("0005", "Only a RETURNING box can become IDLE", HttpStatus.BAD_REQUEST ),
    LOADED_STATE_ERROR("0006", "Only a LOADING box can be marked LOADED", HttpStatus.BAD_REQUEST),
    BOX_NOT_FOUND("0007", "Box not found", HttpStatus.NOT_FOUND),
    ITEMS_NOT_FOUND("0008", "Items not found, box cannot be loaded", HttpStatus.NOT_FOUND),
    INVALID_PARAMETERS("0009", "Invalid request parameter.", HttpStatus.BAD_REQUEST),
    MINIMUM_WEIGHT_ERROR("0010", "Minimum box weight limit exceeded", HttpStatus.BAD_REQUEST ),
    MAXIMUM_WEIGHT_ERROR("0011", "Maximum box weight limit exceeded", HttpStatus.BAD_REQUEST ),
    INVALID_KEY("0012", "Invalid key supplied", HttpStatus.BAD_REQUEST ),
    ITEM_CODE_ALREADY_EXISTS("0013", "A new item code already exists", HttpStatus.CONFLICT )
    ;

    private final String code;
    private final String description;
    private final HttpStatus httpStatus;

    ResponseCode(String code, String description, HttpStatus httpStatus) {
        this.code = code;
        this.description = description;
        this.httpStatus = httpStatus;
    }
}
