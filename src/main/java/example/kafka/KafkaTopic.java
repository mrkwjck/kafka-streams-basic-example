package example.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopic {

    INPUT_ORDERS("ORDERS",false),
    INPUT_PRODUCTS("PRODUCTS",true),
    OUTPUT_SHIPMENTS("SHIPMENTS",false),
    OUTPUT_SALES("SALES",true);

    private final String topicName;
    private final boolean compact;

}
