package miju.com.robodelivery;

import miju.com.robodelivery.dto.requests.ItemRequest;
import miju.com.robodelivery.dto.requests.LoadItemsRequest;
import miju.com.robodelivery.dto.responses.BoxResponse;
import miju.com.robodelivery.dto.responses.ItemResponse;
import miju.com.robodelivery.entities.Box;
import miju.com.robodelivery.entities.Item;
import miju.com.robodelivery.exceptions.APIErrorException;
import miju.com.robodelivery.helpers.SystemProperties;
import miju.com.robodelivery.repositories.BoxRepository;
import miju.com.robodelivery.repositories.ItemRepository;
import miju.com.robodelivery.services.BoxService;
import miju.com.robodelivery.services.ItemDaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BoxServiceTest {
    @Autowired BoxService boxService;
    @Autowired BoxRepository boxRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired ItemDaoService itemDaoService;
    @Autowired
    private SystemProperties systemProperties;

    @Test
    void returnsAllCatalogItems() {
        itemRepository.save(new Item("shoe", new BigDecimal("55"), "CATALOG_ITEM"));

        List<ItemResponse> response = (List<ItemResponse>) itemDaoService.findAll().getBody().getData();

        assertTrue(response.stream().anyMatch(item -> item.getCode().equals("CATALOG_ITEM")));
    }

    @Test
    void savesOneDuplicateNewCodesButLoadsEveryItemTest() {
        boxRepository.save(new Box("DUPLICATE_NEW", new BigDecimal("500"), 80));

        BoxResponse response = (BoxResponse) boxService.load(load("DUPLICATE_NEW", 55, "SH_BAL_45", 55, "SH_BAL_45"))
                .getBody().getData();

        assertEquals(0, new BigDecimal("110").compareTo(response.getLoadedWeight()));
        assertEquals(2, response.getItems().size());
        assertTrue(itemRepository.existsById("SH_BAL_45"));
    }

    @Test
    void rejectsAnOverweightLoad_rejectsSavingTheNewItemTest() {
        boxRepository.save(new Box("OVERWEIGHT", new BigDecimal("100"), 80));

        APIErrorException exception = assertThrows(APIErrorException.class,
                () -> boxService.load(load("OVERWEIGHT", 101, "HEAVY_1")));

        assertEquals("0004", exception.getStatusCode());
        assertFalse(itemRepository.existsById("HEAVY_1"));
    }

    @Test
    void rejectsLoadingWhenBatteryIsBelowMinimumLoadingCapacityPercentTest() {
        boxRepository.save(new Box("LOW_BATTERY", new BigDecimal("500"), systemProperties.getMinimumLoadingBatteryCapacity()-1));

        APIErrorException exception = assertThrows(APIErrorException.class,
                () -> boxService.load(load("LOW_BATTERY", 10, "LIGHT_1")));

        assertEquals("0003", exception.getStatusCode());
    }

    @Test
    void rejectsTheEntireLoadWhenAnyRequestedExistingCodeIsMissingTest() {
        boxRepository.save(new Box("MISSING_CODE", new BigDecimal("500"), 80));
        itemRepository.save(new Item("shoe", new BigDecimal("55"), "EXISTING_1"));

        LoadItemsRequest request = new LoadItemsRequest();
        request.setTxref("MISSING_CODE");
        request.setCodes(List.of("EXISTING_1", "UNKNOWN_1"));

        APIErrorException exception = assertThrows(APIErrorException.class, () -> boxService.load(request));

        assertEquals("0008", exception.getStatusCode());
        assertEquals("Items not found, box cannot be loaded", exception.getMessage());
    }

    @Test
    void loadsEveryRequestedItemsWhenNewItemsAreValidAndExistingCodesArePresentTest() {
        boxRepository.save(new Box("EXISTING_CODES", new BigDecimal("500"), 80));
        itemRepository.save(new Item("shoe", new BigDecimal("55"), "EXISTING_2"));

        LoadItemsRequest request = new LoadItemsRequest();
        request.setTxref("EXISTING_CODES");
        request.setCodes(List.of("EXISTING_2", "EXISTING_2"));

        BoxResponse response = (BoxResponse) boxService.load(request).getBody().getData();

        assertEquals(2, response.getItems().size());
        assertEquals(0, new BigDecimal("110").compareTo(response.getLoadedWeight()));
    }

    private LoadItemsRequest load(String txref, int firstWeight, String firstCode) {
        return load(txref, firstWeight, firstCode, null, null);
    }

    private LoadItemsRequest load(String txref, int firstWeight, String firstCode, Integer secondWeight, String secondCode) {
        List<ItemRequest> items = secondWeight == null
                ? List.of(item(firstWeight, firstCode))
                : List.of(item(firstWeight, firstCode), item(secondWeight, secondCode));
        LoadItemsRequest request = new LoadItemsRequest();
        request.setTxref(txref);
        request.setItems(items);
        return request;
    }

    private ItemRequest item(int weight, String code) {
        ItemRequest item = new ItemRequest();
        item.setName("shoe");
        item.setWeight(BigDecimal.valueOf(weight));
        item.setCode(code);
        return item;
    }
}
