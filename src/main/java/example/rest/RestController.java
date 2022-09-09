package example.rest;

import example.kafka.OrderPublisher;
import example.kafka.SalesDataProvider;
import example.model.ShipmentProduct;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
class RestController {

    private final OrderPublisher orderPublisher;
    private final SalesDataProvider salesDataProvider;

    @ApiOperation(value = "create order", tags = "Example")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CreateOrderResponse.class),
    })
    @PostMapping(path = "/orders")
    public void createOrder(@Valid @RequestBody final CreateOrderRequest request) {
        orderPublisher.publishOrder(CreateOrderRequestMapper.toOrder(request));
    }

    @ApiOperation(value = "get sales data", tags = "Example")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ShipmentsResponse.class),
    })
    @GetMapping(path = "/sales")
    public List<ShipmentProduct> getSales() {
        return salesDataProvider.getProductsSales();
    }

}
