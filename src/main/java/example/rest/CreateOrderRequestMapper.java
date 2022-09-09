package example.rest;

import example.model.Order;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
class CreateOrderRequestMapper {

    public static Order toOrder(final CreateOrderRequest request) {
        return new Order(UUID.randomUUID().toString(), request.getItem(), request.getRecipient());
    }

}
