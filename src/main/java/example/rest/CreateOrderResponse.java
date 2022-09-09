package example.rest;

import lombok.Value;

import java.util.UUID;

@Value
class CreateOrderResponse {

    UUID orderId;

}
