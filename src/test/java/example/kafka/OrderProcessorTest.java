package example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.model.Item;
import example.model.Order;
import example.model.Recipient;
import example.model.Shipment;
import example.model.ShipmentProduct;
import example.model.StockProduct;
import example.utils.ObjectMapperFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProcessorTest {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();
    private static  final JsonSerde<Order> ORDER_EVENT_SERDE = new JsonSerde<>(Order.class, OBJECT_MAPPER);
    private static final JsonSerde<StockProduct> STOCK_PRODUCT_SERDE = new JsonSerde<>(StockProduct.class, OBJECT_MAPPER);
    private static final JsonSerde<Shipment> SHIPMENT_SERDE = new JsonSerde<>(Shipment.class, OBJECT_MAPPER);

    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Order> ordersTopic;
    private TestInputTopic<String, StockProduct> stockProductsTopic;
    private TestOutputTopic<String, Shipment> shipmentsTopic;

    @BeforeEach
    void setup() {
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final OrderProcessor orderProcessor = new OrderProcessor(streamsBuilder, OBJECT_MAPPER);

        orderProcessor.buildTopology();

        topologyTestDriver = new TopologyTestDriver(streamsBuilder.build());
        ordersTopic = topologyTestDriver.createInputTopic(KafkaTopic.INPUT_ORDERS.getTopicName(),
                Serdes.String().serializer(), ORDER_EVENT_SERDE.serializer());
        stockProductsTopic = topologyTestDriver.createInputTopic(KafkaTopic.INPUT_PRODUCTS.getTopicName(),
                Serdes.String().serializer(), STOCK_PRODUCT_SERDE.serializer());
        shipmentsTopic = topologyTestDriver.createOutputTopic(KafkaTopic.OUTPUT_SHIPMENTS.getTopicName(),
                Serdes.String().deserializer(), SHIPMENT_SERDE.deserializer());
    }

    @AfterEach
    void cleanup() {
        topologyTestDriver.close();
    }

    @Test
    void should_create_shipment_when_ordering_existing_product() {

        //given

        final String orderId = UUID.randomUUID().toString();

        //when

        stockProductsTopic.pipeInput("1", product("1"));
        ordersTopic.pipeInput(UUID.randomUUID().toString(), order(orderId, "1"));

        //then

        final List<Pair<String, Shipment>> results = shipmentsTopic.readRecordsToList().stream()
                .map(record -> Pair.of(record.key(), record.value()))
                .toList();

        assertThat(results)
                .hasSize(1)
                .contains(Pair.of(orderId, shipment(orderId)));

    }

    @Test
    void should_not_create_shipment_when_ordering_nonexistent_product() {

        //given

        final String orderId = UUID.randomUUID().toString();

        //when

        stockProductsTopic.pipeInput("2", product("2"));
        ordersTopic.pipeInput(orderId, order(orderId, "1"));

        //then

        final List<Pair<String, Shipment>> results = shipmentsTopic.readRecordsToList().stream()
                .map(record -> Pair.of(record.key(), record.value()))
                .toList();

        assertThat(results).isEmpty();

    }

    private static StockProduct product(final String productId) {
        return new StockProduct(productId, "test product");
    }

    private static Order order(final String orderId, final String productId) {
        final Item product = new Item(productId, 100);
        final Recipient recipient = new Recipient("John", "Doe");
        return new Order(orderId, product, recipient);
    }

    private static Shipment shipment(final String orderId) {
        final ShipmentProduct product = new ShipmentProduct("1", "test product", 100);
        final Recipient recipient = new Recipient("John", "Doe");
        return new Shipment(orderId, product, recipient);
    }

}