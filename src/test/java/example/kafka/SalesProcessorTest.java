package example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.model.Recipient;
import example.model.Shipment;
import example.model.ShipmentProduct;
import example.utils.ObjectMapperFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SalesProcessorTest {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();
    private static final JsonSerde<Shipment> SHIPMENT_SERDE = new JsonSerde<>(Shipment.class, OBJECT_MAPPER);

    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Shipment> shipmentsTopic;

    @BeforeEach
    void setup() {
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final SalesProcessor salesProcessor = new SalesProcessor(streamsBuilder, OBJECT_MAPPER);

        salesProcessor.buildTopology();

        topologyTestDriver = new TopologyTestDriver(streamsBuilder.build());
        shipmentsTopic = topologyTestDriver.createInputTopic(KafkaTopic.OUTPUT_SHIPMENTS.getTopicName(),
                Serdes.String().serializer(), SHIPMENT_SERDE.serializer());
    }

    @AfterEach
    void cleanup() {
        topologyTestDriver.close();
    }

    @Test
    void should_sum_product_sales_from_multiple_shipments() {

        //given

        final String order1Id = UUID.randomUUID().toString();
        final String order2Id = UUID.randomUUID().toString();
        final String order3Id = UUID.randomUUID().toString();

        //when

        shipmentsTopic.pipeInput(order1Id, shipment(order1Id, "1", 100));
        shipmentsTopic.pipeInput(order2Id, shipment(order2Id, "1", 100));
        shipmentsTopic.pipeInput(order3Id, shipment(order2Id, "2", 250));

        //then

        final KeyValueStore<String, ShipmentProduct> salesStore =
                topologyTestDriver.getKeyValueStore(SalesProcessor.SALES_STORE_NAME);

        assertThat(salesStore.get("1").getOrderedQuantity()).isEqualTo(200);
        assertThat(salesStore.get("2").getOrderedQuantity()).isEqualTo(250);
    }

    private static Shipment shipment(final String orderId, final String productId, final int orderedQuantity) {
        final ShipmentProduct product = new ShipmentProduct(productId, "test product", orderedQuantity);
        final Recipient recipient = new Recipient("John", "Doe");
        return new Shipment(orderId, product, recipient);
    }

}