package example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Profile("!test")
@Configuration
@EnableKafkaStreams
class KafkaStreamsConfiguration {

    private static final String SALES_PROCESSOR_BEAN_NAME = "salesProcessor";

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    org.springframework.kafka.config.KafkaStreamsConfiguration kafkaStreamsConfiguration(final KafkaProperties kafkaProperties) {
        final Map<String, Object> parameters = Map.of(
                StreamsConfig.APPLICATION_ID_CONFIG, kafkaProperties.getStreams().getApplicationId(),
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers(),
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName(),
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName(),
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        );
        return new org.springframework.kafka.config.KafkaStreamsConfiguration(parameters);
    }

    @Bean
    OrderProcessor orderProcessor(final StreamsBuilder streamsBuilder, final ObjectMapper objectMapper) {
        return new OrderProcessor(streamsBuilder, objectMapper);
    }

    @Bean(name = SALES_PROCESSOR_BEAN_NAME)
    SalesProcessor salesProcessor(final StreamsBuilder streamsBuilder, final ObjectMapper objectMapper) {
        return new SalesProcessor(streamsBuilder, objectMapper);
    }

    @Bean
    @DependsOn(SALES_PROCESSOR_BEAN_NAME)
    SalesDataProvider salesDataProvider(final StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        return new SalesDataProviderImpl(streamsBuilderFactoryBean);
    }

}
