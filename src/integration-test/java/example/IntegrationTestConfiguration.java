package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.kafka.KafkaTopic;
import example.kafka.SalesDataProvider;
import example.model.Order;
import example.model.ShipmentProduct;
import example.model.StockProduct;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Profile("test")
@Configuration
class IntegrationTestConfiguration {

    @Bean
    Consumer<String, Order> ordersTopicConsumer(final KafkaProperties kafkaProperties,
                                                final ObjectMapper objectMapper) {
        final Consumer<String, Order> consumer =
                getConsumerFactory(Order.class, kafkaProperties, objectMapper).createConsumer();
        consumer.subscribe(Collections.singletonList(KafkaTopic.INPUT_ORDERS.getTopicName()));
        return consumer;
    }

    @Bean
    Consumer<String, StockProduct> productsTopicConsumer(final KafkaProperties kafkaProperties,
                                                         final ObjectMapper objectMapper) {
        final Consumer<String, StockProduct> consumer =
                getConsumerFactory(StockProduct.class, kafkaProperties, objectMapper).createConsumer();
        consumer.subscribe(Collections.singletonList(KafkaTopic.INPUT_PRODUCTS.getTopicName()));
        return consumer;
    }

    @Bean
    SalesDataProvider salesDataProvider() {
        final List<ShipmentProduct> mockSalesData = List.of(
                new ShipmentProduct("1", "test product 1", 300),
                new ShipmentProduct("2", "test product 2", 100)
        );
        final SalesDataProvider salesDataProvider = mock(SalesDataProvider.class);
        when(salesDataProvider.getProductsSales()).thenReturn(mockSalesData);
        return salesDataProvider;
    }

    private static Map<String, Object> getKafkaConsumerProperties(final KafkaProperties kafkaProperties) {
        final Map<String, Object> consumerProperties = new HashMap<>();
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProperties.putAll(kafkaProperties.buildConsumerProperties());
        return consumerProperties;
    }

    private static <T> DefaultKafkaConsumerFactory<String, T> getConsumerFactory(final Class<T> classToDeserialize,
                                                                                 final KafkaProperties kafkaProperties,
                                                                                 final ObjectMapper objectMapper) {
        return new DefaultKafkaConsumerFactory<>(getKafkaConsumerProperties(kafkaProperties),
                new StringDeserializer(), new JsonDeserializer<>(classToDeserialize, objectMapper));
    }

}
