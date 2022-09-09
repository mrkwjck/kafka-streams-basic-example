package example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.model.Recipient;
import example.model.Shipment;
import example.model.ShipmentProduct;
import example.utils.ObjectMapperFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static example.kafka.SalesProcessor.SALES_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalesDataProviderImplTest {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();
    private static final JsonSerde<Shipment> SHIPMENT_SERDE = new JsonSerde<>(Shipment.class, OBJECT_MAPPER);

    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
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

        final KafkaStreams kafkaStreams = mock(KafkaStreams.class);
        when(kafkaStreams.store(Mockito.any())).thenReturn(topologyTestDriver.getKeyValueStore(SALES_STORE_NAME));
        streamsBuilderFactoryBean = mock(StreamsBuilderFactoryBean.class);
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
    }

    @AfterEach
    void cleanup() {
        topologyTestDriver.close();
    }

    @Test
    void should_sum_shipped_products_quantities() {

        //given

        final SalesDataProvider salesDataProvider = new SalesDataProviderImpl(streamsBuilderFactoryBean);
        final UUID shipmentId1 = UUID.randomUUID();
        final UUID shipmentId2 = UUID.randomUUID();
        final UUID shipmentId3 = UUID.randomUUID();
        final List<Pair<UUID, Shipment>> shipments = List.of(
                Pair.of(shipmentId1, shipment(shipmentId1, "1", 100)),
                Pair.of(shipmentId1, shipment(shipmentId1, "1", 150)),
                Pair.of(shipmentId2, shipment(shipmentId2, "2", 200)),
                Pair.of(shipmentId3, shipment(shipmentId3, "3", 450)),
                Pair.of(shipmentId3, shipment(shipmentId3, "3", 5))
        );

        //when

        shipments.forEach(shipment -> shipmentsTopic.pipeInput(shipment.getKey().toString(), shipment.getValue()));

        //then

        final Map<String, Integer> sales = salesDataProvider.getProductsSales()
                .stream()
                .collect(Collectors.toMap(ShipmentProduct::getId, ShipmentProduct::getOrderedQuantity));

        assertThat(sales)
                .containsEntry("1", 250)
                .containsEntry("2", 200)
                .containsEntry("3", 455);

    }

    private static Shipment shipment(final UUID shipmentId, final String productId, final int productQuantity) {
        final ShipmentProduct product = new ShipmentProduct(productId, "test product", productQuantity);
        final Recipient recipient = new Recipient("John", "Doe");
        return new Shipment(shipmentId.toString(), product, recipient);
    }

}