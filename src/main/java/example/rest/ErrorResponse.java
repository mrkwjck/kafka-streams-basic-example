package example.rest;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ErrorResponse {

    static final String SALES_STATE_STORE_NOT_READY = "SALES_STATE_STORE_NOT_READY";
    static final String INVALID_ORDER_REQUEST = "INVALID_ORDER_REQUEST";
    static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    String code;
    String message;

}
