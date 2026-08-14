package miju.com.robodelivery.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miju.com.robodelivery.dto.requests.ItemRequest;
import miju.com.robodelivery.entities.Item;
import miju.com.robodelivery.helpers.ApiHelper;
import miju.com.robodelivery.repositories.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static miju.com.robodelivery.enums.ResponseCode.ITEMS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ItemDaoService {
    private final ItemRepository repository;
    private final ApiHelper apiHelper;


    public List<Item> findAllItemsByCode(List<String> itemCodes) {
       Map<String, Item> itemsByCode = repository.findByCodeIn(itemCodes).stream()
               .collect(Collectors.toMap(Item::getCode, Function.identity()));
       // Resolve from the request rather than the database result so repeated
       // codes intentionally produce repeated box entries.
       return itemCodes.stream()
               .map(itemsByCode::get)
               .filter(java.util.Objects::nonNull)
               .toList();
    }

    public List<Item> createNewItems(List<ItemRequest> items) {
        return repository.saveAll(items.stream().map(itemRequest -> new Item(itemRequest.getName(),itemRequest.getWeight(), itemRequest.getCode())).toList());
    }

    public boolean anyItemExists(List<String> itemCodes) {
        return repository.existsByCodeIn(itemCodes);
    }
}
