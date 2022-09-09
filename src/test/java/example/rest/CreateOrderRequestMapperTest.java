package example.rest;

import example.model.Item;
import example.model.Order;
import example.model.Recipient;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CreateOrderRequestMapperTest {

    @Test
    void should_map_create_order_request() {

        //given

        final CreateOrderRequest request = getCreateOrderRequest("1", 100, "John", "Doe");

        //when

        final Order order = CreateOrderRequestMapper.toOrder(request);

        //then

        assertThat(order)
                .hasFieldOrPropertyWithValue("recipient", request.getRecipient())
                .hasFieldOrPropertyWithValue("item", request.getItem());
        assertDoesNotThrow(() -> UUID.fromString(order.getId()),
                "Invalid orderId set in mapped order - should be valid UUID");
    }

    private static CreateOrderRequest getCreateOrderRequest(final String itemId,
                                                            final int quantity,
                                                            final String recipientFirstName,
                                                            final String recipientLastName) {
        final Item item = new Item(itemId, quantity);
        final Recipient recipient = new Recipient(recipientFirstName, recipientLastName);
        return new CreateOrderRequest(item, recipient);
    }

}