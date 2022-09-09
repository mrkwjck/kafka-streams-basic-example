package example.kafka;

import example.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class OrderPublisher {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    public void publishOrder(final Order order) {
        log.info("Publishing order {} to kafka topic {}", order, KafkaTopic.INPUT_ORDERS.getTopicName());
        kafkaTemplate.send(KafkaTopic.INPUT_ORDERS.getTopicName(), order.getId(), order);
    }

}
