package miju.com.robodelivery.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miju.com.robodelivery.dto.responses.APIResponse;
import miju.com.robodelivery.dto.responses.BatteryCapacity;
import miju.com.robodelivery.entities.Box;
import miju.com.robodelivery.helpers.ApiHelper;
import miju.com.robodelivery.helpers.SystemProperties;
import miju.com.robodelivery.repositories.BoxRepository;
import miju.com.robodelivery.enums.BoxState;
import miju.com.robodelivery.dto.requests.CreateBoxRequest;
import miju.com.robodelivery.dto.responses.BoxResponse;
import miju.com.robodelivery.dto.responses.ItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static miju.com.robodelivery.enums.ResponseCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxDaoService {
    private final BoxRepository repository;
    private final ApiHelper apiHelper;
    private final SystemProperties systemProperties;


    @Transactional
    public ResponseEntity<APIResponse<Object>> create(CreateBoxRequest request) {
        log.info("Started box create service");
        if (repository.existsByTxref(request.getTxref())) throw apiHelper.getException(TXREF_ALREADY_EXISTS);
        if (request.getWeightLimit() < systemProperties.getMinimumBoxWeightLimit()) throw apiHelper.getException(MINIMUM_WEIGHT_ERROR);
        if (request.getWeightLimit() > systemProperties.getMaximumBoxWeightLimit()) throw apiHelper.getException(MAXIMUM_WEIGHT_ERROR);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.builder()
                .data(BoxResponse.from(repository.save(new Box(request.getTxref(), BigDecimal.valueOf(request.getWeightLimit()), request.getBatteryCapacity()))))
                .statusCode(OK.getCode())
                .statusMessage(OK.getDescription())
                .build());
    }


    @Transactional(readOnly = true)
    public ResponseEntity<APIResponse<Object>> items(String txref) {
        log.info("Started items retrieval service");

        List<ItemResponse> itemResponses = getBoxByTxref(txref).getItems().stream().map(ItemResponse::from).toList();
        return ResponseEntity.ok(
                APIResponse.builder()
                        .data(itemResponses)
                        .statusCode(OK.getCode())
                        .statusMessage(OK.getDescription())
                        .build());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<APIResponse<Object>> available() {
        log.info("Retrieval of available boxes");

        List<BoxResponse> boxResponses =  repository.findByStateIn(List.of(BoxState.IDLE, BoxState.LOADING)).stream()
                .filter(b -> b.getBatteryCapacity() >= systemProperties.getMinimumLoadingBatteryCapacity() && b.loadedWeight().compareTo(b.getWeightLimit()) < 0)
                .map(BoxResponse::from).toList();
        return ResponseEntity.ok(
                APIResponse.builder()
                        .data(boxResponses)
                        .statusCode(OK.getCode())
                        .statusMessage(OK.getDescription())
                        .build());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<APIResponse<Object>> battery(String txref) {
        log.info("Started retrieval of battery capacity for : {}", txref);

        return ResponseEntity.ok(APIResponse.builder()
                .statusCode(OK.getCode())
                .data(BatteryCapacity.builder()
                .boxTxref(txref)
                .capacity(getBatteryCapacity(txref))
                .build())
                .statusMessage(OK.getDescription())
                .build());
    }

    @Transactional
    public ResponseEntity<APIResponse<Object>> loaded(String txref) {
        log.info("Started service to mark box as loaded");

        Box box = getBoxByTxref(txref);
        if (box.getState() != BoxState.LOADING) throw apiHelper.getException(LOADED_STATE_ERROR);
        box.setState(BoxState.LOADED);
        return ResponseEntity.ok(
                APIResponse.builder()
                        .data(BoxResponse.from(box))
                        .statusCode(OK.getCode())
                        .statusMessage(OK.getDescription())
                        .build());
    }

    /** Callback endpoint for the physical box after it has completed its return journey. */
    @Transactional
    public ResponseEntity<APIResponse<Object>> returned(String txref) {
        log.info("Started box returned service");

        Box box = getBoxByTxref(txref);
        if (box.getState() != BoxState.RETURNING) throw apiHelper.getException(IDLE_STATE_ERROR);
        box.setState(BoxState.IDLE);
        return ResponseEntity.ok(
                APIResponse.builder()
                        .data(BoxResponse.from(box))
                        .statusCode(OK.getCode())
                        .statusMessage(OK.getDescription())
                        .build());
    }

    public Box getBoxByTxref(String txref) {
        log.info("Retrieval of items for : {}", txref);
        return repository.findByTxref(txref).orElseThrow(() -> apiHelper.getException(BOX_NOT_FOUND));
    }

    private int getBatteryCapacity(String txref) {
        return getBoxByTxref(txref).getBatteryCapacity();
    }
}
