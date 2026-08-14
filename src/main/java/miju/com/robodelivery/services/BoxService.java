package miju.com.robodelivery.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miju.com.robodelivery.dto.requests.ItemRequest;
import miju.com.robodelivery.dto.requests.LoadItemsRequest;
import miju.com.robodelivery.dto.responses.APIResponse;
import miju.com.robodelivery.dto.responses.BoxResponse;
import miju.com.robodelivery.entities.Box;
import miju.com.robodelivery.entities.Item;
import miju.com.robodelivery.enums.BoxState;
import miju.com.robodelivery.helpers.ApiHelper;
import miju.com.robodelivery.helpers.SystemProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static miju.com.robodelivery.enums.ResponseCode.*;
import static miju.com.robodelivery.enums.ResponseCode.OK;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxService {
    private final BoxDaoService boxDaoService;
    private final ItemDaoService itemDaoService;
    private final ApiHelper apiHelper;
    private final SystemProperties systemProperties;


    @Transactional
    public ResponseEntity<APIResponse<Object>> load(LoadItemsRequest request) {
        log.info("Started box loading service");
        Box box = boxDaoService.getBoxByTxref(request.getTxref());
        if (box.getState() != BoxState.IDLE && box.getState() != BoxState.LOADING) throw apiHelper.getException(LOADING_ERROR);
        if (box.getBatteryCapacity() < systemProperties.getMinimumLoadingBattery()) throw apiHelper.getException(BATTERY_LOW_ERROR);

        List<ItemRequest> newItemRequests = request.getItems() == null ? List.of() : request.getItems();
        List<String> existingItemCodes = request.getCodes() == null ? List.of() : request.getCodes();
        if (newItemRequests.isEmpty() && existingItemCodes.isEmpty()) throw apiHelper.getException(INVALID_PARAMETERS);

        List<ItemRequest> uniqueNewItemRequests = distinctNewItemsByCode(newItemRequests);
        validateNewItemCodes(uniqueNewItemRequests);

        List<Item> items = new ArrayList<>();
        if (!existingItemCodes.isEmpty()) {
            items.addAll(itemDaoService.findAllItemsByCode(existingItemCodes));
        }

        BigDecimal incomingWeight = items.stream().map(Item::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(newItemRequests.stream().map(ItemRequest::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add));
        if (items.isEmpty() && newItemRequests.isEmpty()) throw apiHelper.getException(ITEMS_NOT_FOUND);
        if (box.loadedWeight().add(incomingWeight).compareTo(box.getWeightLimit()) > 0) throw apiHelper.getException(WEIGHT_LIMIT_EXCEEDED);

        // Persist each new catalog code once, then retain every request occurrence
        // when adding items to the box.
        if (!uniqueNewItemRequests.isEmpty()) {
            Map<String, Item> savedItemsByCode = itemDaoService.createNewItems(uniqueNewItemRequests).stream()
                    .collect(Collectors.toMap(Item::getCode, Function.identity()));
            newItemRequests.stream()
                    .map(itemRequest -> savedItemsByCode.get(itemRequest.getCode()))
                    .forEach(items::add);
        }
        items.forEach(box::addItem);
        if (box.loadedWeight().compareTo(box.getWeightLimit()) == 0) {
            box.setState(BoxState.LOADED);
        } else {
            box.setState(BoxState.LOADING);
        }
        return ResponseEntity.ok(APIResponse.builder()
                .data(BoxResponse.from(box))
                .statusCode(OK.getCode())
                .statusMessage(OK.getDescription())
                .build());
    }

    private void validateNewItemCodes(List<ItemRequest> newItems) {
        List<String> codes = newItems.stream().map(ItemRequest::getCode).toList();
        if (!codes.isEmpty() && itemDaoService.anyItemExists(codes)) {
            throw apiHelper.getException(ITEM_CODE_ALREADY_EXISTS);
        }
    }

    private List<ItemRequest> distinctNewItemsByCode(List<ItemRequest> newItems) {
        return new ArrayList<>(newItems.stream()
                .collect(Collectors.toMap(ItemRequest::getCode, Function.identity(), (first, ignored) -> first))
                .values());
    }
}
