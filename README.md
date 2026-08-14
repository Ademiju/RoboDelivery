# RoboDelivery API

RoboDelivery simulates digital delivery boxes and the items loaded into them. The API uses JSON and an in-memory H2 database. Two sample boxes are seeded each time the application starts.

## Data model and loading behaviour

`Box` and `Item` are independent entities connected by the `box_items` join table. An item is not owned by a box, so an existing catalog item can be loaded into any eligible box. A load request may also repeat an existing item code; each occurrence is added to the box and contributes to its loaded weight.

New items are supplied in the `items` array and are persisted only after the box state, battery, and total-weight checks pass. Repeated new-item codes in one request are saved once in the item catalog but every occurrence is loaded into the box. A new-item code must not already be present in the catalog; to use an existing item, send its code in `codes` instead.

Weights are measured in grams. A box may be loaded only when it is `IDLE` or `LOADING`, its battery is at least 25%, and the load will not exceed its configured weight limit.

## Build and run

Requires Java 21 and Maven.

```powershell
mvn clean package
mvn spring-boot:run
mvn test
```

The API is available at `http://localhost:8080`. The development H2 console is at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:robodelivery`.

## API

All successful responses use this envelope:

```json
{
  "data": {},
  "statusCode": "0000",
  "statusMessage": "Success."
}
```

### Create a box

`POST /api/robo/box`

Request:

```json
{
  "txref": "DELIVERY_001",
  "weightLimit": 500,
  "batteryCapacity": 85
}
```

Response (`200 OK`):

```json
{
  "data": {
    "txref": "DELIVERY_001",
    "weightLimit": 500.00,
    "loadedWeight": 0,
    "remainingWeight": 500.00,
    "batteryCapacity": 85,
    "state": "IDLE",
    "items": []
  },
  "statusCode": "0000",
  "statusMessage": "Success."
}
```

### Load items into a box

`POST /api/robo/items`

The request body must contain `txref` and at least one of `items` or `codes`.

Create and load new catalog items:

```json
{
  "txref": "DELIVERY_001",
  "items": [
    { "name": "medical-kit", "weight": 120, "code": "MEDICAL_KIT_1" },
    { "name": "food_pack", "weight": 80, "code": "FOOD_PACK_2" }
  ]
}
```

Load existing items by code (the same code may be repeated):

```json
{
  "txref": "DELIVERY_001",
  "codes": ["MEDICAL_KIT_1", "MEDICAL_KIT_1"]
}
```

New and existing items can be mixed in one request:

```json
{
  "txref": "DELIVERY_001",
  "items": [
    { "name": "charger", "weight": 50, "code": "CHARGER_1" }
  ],
  "codes": ["MEDICAL_KIT_1"]
}
```

Example successful response for the repeated-code request (`200 OK`):

```json
{
  "data": {
    "txref": "DELIVERY_001",
    "weightLimit": 500.00,
    "loadedWeight": 240.00,
    "remainingWeight": 260.00,
    "batteryCapacity": 85,
    "state": "LOADING",
    "items": [
      { "name": "medical-kit", "weight": 120.00, "code": "MEDICAL_KIT_1" },
      { "name": "medical-kit", "weight": 120.00, "code": "MEDICAL_KIT_1" }
    ]
  },
  "statusCode": "0000",
  "statusMessage": "Success."
}
```

If the resulting load equals the box weight limit exactly, the state is returned as `LOADED`; otherwise it is `LOADING`.

### List a box's items

`GET /api/robo/items/{txref}`

Response (`200 OK`):

```json
{
  "data": [
    { "name": "medical-kit", "weight": 120.00, "code": "MEDICAL_KIT_1" }
  ],
  "statusCode": "0000",
  "statusMessage": "Success."
}
```

### List available boxes

`GET /api/robo/boxes`

Returns boxes that are `IDLE` or `LOADING`, have sufficient battery, and still have capacity.

### Read battery capacity

`GET /api/robo/battery/{txref}`

Response (`200 OK`):

```json
{
  "data": { "boxTxref": "DELIVERY_001", "capacity": 85 },
  "statusCode": "0000",
  "statusMessage": "Success."
}
```

### Mark a box as loaded

`PUT /api/robo/loaded?txref=DELIVERY_001`

The box must currently be `LOADING`.

### Successful-delivery return webhook

`PUT /api/robo/webhook?txref=DELIVERY_001`

This endpoint simulates a successful-delivery callback from the physical box or delivery integration. After a successful delivery, the box is expected to be in the `RETURNING` state. The webhook marks it `IDLE`, making it eligible to appear in the available-boxes list and to receive new load requests again.

Include the `delivery-auth` request header with the configured webhook key. The header mimics authentication used by a real webhook sender and prevents unauthenticated callers from changing a box state.

Example request:

```http
PUT /api/robo/webhook?txref=DELIVERY_001
delivery-auth: Robo-delivery-T5wre7252jTTUdst
```

The request succeeds only when the box is `RETURNING`. A box in any other state receives `400 Bad Request`.

## Error responses

Business-rule failures use this shape:

```json
{
  "statusMessage": "A new item code already exists",
  "errorCode": "0013",
  "timestamp": "2026-08-14 12:00:00",
  "path": "/api/robo/items",
  "method": "POST"
}
```

Common outcomes are `400 Bad Request` for invalid state, low battery, an empty load request, or a weight-limit breach; `404 Not Found` for an unknown box or when no requested existing items are found; and `409 Conflict` when a new-item code is already in the catalog or a box transaction reference already exists. Bean-validation failures return `400 Bad Request` with a `message` and an `errors` object.
