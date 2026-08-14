package miju.com.robodelivery;

import miju.com.robodelivery.dto.requests.ItemRequest;
import miju.com.robodelivery.dto.requests.LoadItemsRequest;
import miju.com.robodelivery.dto.responses.BoxResponse;
import miju.com.robodelivery.entities.Box;
import miju.com.robodelivery.exceptions.APIErrorException;
import miju.com.robodelivery.repositories.BoxRepository;
import miju.com.robodelivery.repositories.ItemRepository;
import miju.com.robodelivery.services.BoxService;
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

    @Test
    void savesDuplicateNewCodesOnceButLoadsEveryOccurrence() {
        boxRepository.save(new Box("DUPLICATE_NEW", new BigDecimal("500"), 80));

        BoxResponse response = (BoxResponse) boxService.load(load("DUPLICATE_NEW", 55, "SH_BAL_45", 55, "SH_BAL_45"))
                .getBody().getData();

        assertEquals(new BigDecimal("110"), response.getLoadedWeight());
        assertEquals(2, response.getItems().size());
        assertTrue(itemRepository.existsById("SH_BAL_45"));
    }

    @Test
    void rejectsAnOverweightLoadBeforeSavingTheNewItem() {
        boxRepository.save(new Box("OVERWEIGHT", new BigDecimal("100"), 80));

        APIErrorException exception = assertThrows(APIErrorException.class,
                () -> boxService.load(load("OVERWEIGHT", 101, "HEAVY_1")));

        assertEquals("0004", exception.getStatusCode());
        assertFalse(itemRepository.existsById("HEAVY_1"));
    }

    @Test
    void rejectsLoadingWhenBatteryIsBelowTwentyFivePercent() {
        boxRepository.save(new Box("LOW_BATTERY", new BigDecimal("500"), 24));

        APIErrorException exception = assertThrows(APIErrorException.class,
                () -> boxService.load(load("LOW_BATTERY", 10, "LIGHT_1")));

        assertEquals("0003", exception.getStatusCode());
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
