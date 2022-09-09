package example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.model.Order;
import example.model.Shipment;
import example.model.ShipmentProduct;
import example.model.StockProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.springframework.kafka.support.serializer.JsonSerde;

import javax.annotation.PostConstruct;

@Slf4j
@RequiredArgsConstructor
class OrderProcessor {

    private final StreamsBuilder streamsBuilder;
    private final ObjectMapper objectMapper;

    @PostConstruct
    void buildTopology() {
        final JsonSerde<Order> orderEventSerde= new JsonSerde<>(Order.class, objectMapper);
        final JsonSerde<StockProduct> stockProductSerde = new JsonSerde<>(StockProduct.class, objectMapper);
        final JsonSerde<Shipment> shipmentSerde = new JsonSerde<>(Shipment.class, objectMapper);

        final KStream<String, Order> ordersStream = streamsBuilder.stream(
                KafkaTopic.INPUT_ORDERS.getTopicName(), Consumed.with(Serdes.String(), orderEventSerde));
        final GlobalKTable<String, StockProduct> stockProductsTable = streamsBuilder.globalTable(
                KafkaTopic.INPUT_PRODUCTS.getTopicName(), Materialized.with(Serdes.String(), stockProductSerde));

        ordersStream
                .join(stockProductsTable, getOrderProductKeyMapper(), getOrderProductJoiner())
                .selectKey((shipmentKey, shipmentValue) -> shipmentValue.getId())
                .peek(((shipmentKey, shipmentValue) -> log.info("Publishing shipment {} to topic {}",
                        shipmentValue, KafkaTopic.OUTPUT_SHIPMENTS.getTopicName())))
                .to(KafkaTopic.OUTPUT_SHIPMENTS.getTopicName(), Produced.with(Serdes.String(), shipmentSerde));
    }

    private ValueJoiner<Order, StockProduct, Shipment> getOrderProductJoiner() {
        return (order, product) -> {
            final ShipmentProduct shipmentProduct =
                    new ShipmentProduct(product.getId(), product.getName(), order.getItem().getQuantity());
            return new Shipment(order.getId(), shipmentProduct, order.getRecipient());
        };
    }

    private KeyValueMapper<String, Order, String> getOrderProductKeyMapper() {
        return (orderKey, orderValue) -> orderValue.getItem().getId();
    }

}
