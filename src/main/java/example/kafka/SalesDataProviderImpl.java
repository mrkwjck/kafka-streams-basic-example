package example.kafka;

import example.model.ShipmentProduct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static example.kafka.SalesProcessor.SALES_STORE_NAME;

@Slf4j
@RequiredArgsConstructor
class SalesDataProviderImpl implements SalesDataProvider {

    final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private ReadOnlyKeyValueStore<String, ShipmentProduct> salesStore;

    @SneakyThrows
    @Override
    public List<ShipmentProduct> getProductsSales() {
        final List<ShipmentProduct> shipmentProducts = new ArrayList<>();
        try (final KeyValueIterator<String, ShipmentProduct> salesStoreIterator = getSalesStoreInstance().all()) {
            while (salesStoreIterator.hasNext()) {
                shipmentProducts.add(salesStoreIterator.next().value);
            }
        }
        return shipmentProducts;
    }

    private ReadOnlyKeyValueStore<String, ShipmentProduct> getSalesStoreInstance() {
        if (salesStore == null) {
            salesStore = Optional.ofNullable(streamsBuilderFactoryBean.getKafkaStreams())
                    .map(streams -> streams.store(StoreQueryParameters.fromNameAndType(
                            SALES_STORE_NAME, QueryableStoreTypes.<String, ShipmentProduct>keyValueStore())))
                    .orElseThrow(() -> new IllegalStateException("Kafka streams instance has not been initialized yet"));
        }
        return salesStore;
    }

}
