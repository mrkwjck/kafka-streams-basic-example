package example.kafka;

import example.model.Order;
import example.model.StockProduct;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
@EnableKafka
class KafkaConfiguration {

    private static final String KAFKA_ADMIN_BEAN_NAME = "kafkaAdmin";

    @Bean(name = KAFKA_ADMIN_BEAN_NAME)
    KafkaAdmin kafkaAdmin(final KafkaProperties kafkaProperties) {
        final Map<String, Object> parameters = Map.of(
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers()
        );
        final KafkaAdmin kafkaAdmin = new KafkaAdmin(parameters);
        final List<NewTopic> topics = Stream.of(KafkaTopic.values())
                .map(KafkaConfiguration::createTopicSpecification)
                .toList();
        kafkaAdmin.createOrModifyTopics(topics.toArray(new NewTopic[]{}));
        return kafkaAdmin;
    }

    @Bean
    @DependsOn(KAFKA_ADMIN_BEAN_NAME)
    StockInitiator stockInitiator(final KafkaTemplate<String, StockProduct> kafkaTemplate) {
        return new StockInitiator(kafkaTemplate);
    }

    @Bean
    OrderPublisher orderPublisher(final KafkaTemplate<String, Order> kafkaTemplate) {
        return new OrderPublisher(kafkaTemplate);
    }

    private static NewTopic createTopicSpecification(final KafkaTopic topic) {
        final TopicBuilder topicBuilder = TopicBuilder.name(topic.getTopicName());
        if (topic.isCompact()) {
            topicBuilder.config("cleanup.policy", "compact");
            topicBuilder.config("max.compaction.lag.ms", "100");
            topicBuilder.config("segment.ms", "100");
            topicBuilder.config("min.cleanable.dirty.ratio", "0.001");
        }
        return topicBuilder.build();
    }

}
