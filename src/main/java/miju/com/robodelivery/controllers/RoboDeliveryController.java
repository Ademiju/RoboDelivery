package miju.com.robodelivery.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miju.com.robodelivery.dto.responses.APIResponse;
import miju.com.robodelivery.dto.requests.CreateBoxRequest;
import miju.com.robodelivery.dto.requests.LoadItemsRequest;
import miju.com.robodelivery.helpers.ApiHelper;
import miju.com.robodelivery.services.BoxDaoService;
import miju.com.robodelivery.services.BoxService;
import miju.com.robodelivery.services.ItemDaoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/robo", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RoboDeliveryController {
    private final BoxDaoService boxDaoService;
    private final BoxService boxService;
    private final ItemDaoService itemDaoService;
    private final ApiHelper apiHelper;

    @PostMapping(value = "/box", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse<Object>> createBox(@Valid @RequestBody CreateBoxRequest request) {
        return boxDaoService.create(request);
    }

    @PostMapping(value = "/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse<Object>> loadItemToBox(@Valid @RequestBody LoadItemsRequest request) {
        return boxService.load(request);
    }

    @GetMapping("/items/{txref}")
    public ResponseEntity<APIResponse<Object>> items(@PathVariable String txref) {
        return boxDaoService.items(txref);
    }

    @GetMapping("/items")
    public ResponseEntity<APIResponse<Object>> allItems() {
        return itemDaoService.findAll();
    }

    @GetMapping("/boxes")
    public ResponseEntity<APIResponse<Object>> availableBoxes() {
        return boxDaoService.available();
    }

    @GetMapping("/battery/{txref}")
    public ResponseEntity<APIResponse<Object>> battery(@PathVariable String txref) {
        return boxDaoService.battery(txref);
    }

    @PutMapping("/loaded")
    public ResponseEntity<APIResponse<Object>> markBoxAsLoaded(@RequestParam String txref) {
        return boxDaoService.loaded(txref);
    }

    @PutMapping("/webhook")
    public ResponseEntity<APIResponse<Object>> successfulReturn(@RequestParam String txref, HttpServletRequest request) {
        apiHelper.validateReturnHeader(request.getHeader("delivery-auth"));
        return boxDaoService.returned(txref);
    }
}
