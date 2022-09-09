package example.kafka;

import example.model.StockProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class StockInitiator {

    private final KafkaTemplate<String, StockProduct> kafkaTemplate;

    @PostConstruct
    public void initializeStock() {
        final List<StockProduct> products = List.of(
                new StockProduct("1", "APPLE iPhone 12 64GB 5G"),
                new StockProduct("2", "SAMSUNG 55'' QLED 4K Tizen TV"),
                new StockProduct("3", "APPLE iPad 10.2'' 64GB LTE Wi-Fi"),
                new StockProduct("4", "PHILIPS SenseIQ BHD827/00"),
                new StockProduct("5", "XIAOMI Mi Band 7 Black"),
                new StockProduct("6", "HAUWEI Nova 9 SE 8/128GB"),
                new StockProduct("7", "ZELMER ZIR3000 Stiro"),
                new StockProduct("8", "FIFA 23 PS5"),
                new StockProduct("9", "LENOVO IdeaPad Gaming 3 15IHU6"),
                new StockProduct("10", "BEKO BCNA306E4SN HarvestFresh")
        );
        log.info("Initiating products topic with: {}", products);
        products.forEach(product ->
                kafkaTemplate.send(KafkaTopic.INPUT_PRODUCTS.getTopicName(), product.getId(), product));
    }

}
