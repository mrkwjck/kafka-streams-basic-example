package example.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.IntegrationTest;
import example.model.Item;
import example.model.Recipient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestControllerIT extends IntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_accept_order() throws Exception {

        //given

        final String requestJson = objectMapper.writeValueAsString(
                getCreateOrderRequest("1", 100, "John", "Doe"));

        //expect

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk());

    }

    @Test
    void should_return_bad_request_when_invalid_order_placed() throws Exception {

        //given

        final String requestJson = objectMapper.writeValueAsString(
                getCreateOrderRequest(null, 100, "John", "Doe"));

        //expect

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isBadRequest());


    }

    @Test
    void should_return_sales_data() throws Exception {

        //given

        final String expectedJson = """
                [{"id":"1","name":"test product 1","orderedQuantity":300},
                {"id":"2","name":"test product 2","orderedQuantity":100}]
                """;

        //expect

        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

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