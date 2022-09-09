package example.kafka;

import example.IntegrationTest;
import example.model.Item;
import example.model.Order;
import example.model.Recipient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPublisherIT extends IntegrationTest {

    @Autowired
    private OrderPublisher orderPublisher;

    @Autowired
    private Consumer<String, Order> ordersTopicConsumer;

    @Test
    void should_publish_order_to_topic() {

        //given

        final String orderId = UUID.randomUUID().toString();

        //when

        orderPublisher.publishOrder(order(orderId));

        //then

        final ConsumerRecord<String, Order> orderRecord =
                KafkaTestUtils.getSingleRecord(ordersTopicConsumer, KafkaTopic.INPUT_ORDERS.getTopicName(), 1000);

        assertThat(orderRecord).isNotNull();
        assertThat(orderRecord.value().getId()).isEqualTo(orderId);

    }

    private static Order order(final String orderId) {
        final Item product = new Item("1", 100);
        final Recipient recipient = new Recipient("John", "Doe");
        return new Order(orderId, product, recipient);
    }

}