package example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.model.Shipment;
import example.model.ShipmentProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.kafka.support.serializer.JsonSerde;

import javax.annotation.PostConstruct;

@Slf4j
@RequiredArgsConstructor
class SalesProcessor {

    static final String SALES_STORE_NAME = "sales-store";

    private final StreamsBuilder streamsBuilder;
    private final ObjectMapper objectMapper;

    @PostConstruct
    void buildTopology() {
        final JsonSerde<Shipment> shipmentSerde = new JsonSerde<>(Shipment.class, objectMapper);
        final JsonSerde<ShipmentProduct> shipmentProductSerde = new JsonSerde<>(ShipmentProduct.class, objectMapper);
        final KStream<String, Shipment> shipmentsStream = streamsBuilder
                .stream(KafkaTopic.OUTPUT_SHIPMENTS.getTopicName(), Consumed.with(Serdes.String(), shipmentSerde));
        final Materialized<String, ShipmentProduct, KeyValueStore<Bytes, byte[]>> materialized = Materialized
                .<String, ShipmentProduct, KeyValueStore<Bytes, byte[]>>as(SALES_STORE_NAME)
                .withKeySerde(Serdes.String())
                .withValueSerde(shipmentProductSerde);

        shipmentsStream
                .mapValues(Shipment::getProduct)
                .groupBy(getShipmentProductKeyMapper(), Grouped.with(Serdes.String(), shipmentProductSerde))
                .reduce(SalesProcessor::aggregateProduct, materialized)
                .toStream()
                .peek((productId, product) -> log.info("Publishing sales data {} to topic {}",
                        product, KafkaTopic.OUTPUT_SALES.getTopicName()))
                .to(KafkaTopic.OUTPUT_SALES.getTopicName(), Produced.with(Serdes.String(), shipmentProductSerde));
    }

    private static KeyValueMapper<String, ShipmentProduct, String> getShipmentProductKeyMapper() {
        return (shipmentId, product) -> product.getId();
    }

    private static ShipmentProduct aggregateProduct(final ShipmentProduct product1, final ShipmentProduct product2) {
        return new ShipmentProduct(product2.getId(), product2.getName(),
                product1.getOrderedQuantity() + product2.getOrderedQuantity());
    }

}
