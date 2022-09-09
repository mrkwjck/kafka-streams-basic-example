package example.kafka;

import example.IntegrationTest;
import example.model.StockProduct;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class StockInitiatorIT extends IntegrationTest {

    @Autowired
    private Consumer<String, StockProduct> productsTopicConsumer;

    @Test
    void should_init_products_stock_on_application_startup() {

        //when

        final ConsumerRecords<String, StockProduct> productsRecords =
                KafkaTestUtils.getRecords(productsTopicConsumer, 1000);

        //then

        assertThat(productsRecords.count()).isEqualTo(10);

    }

}