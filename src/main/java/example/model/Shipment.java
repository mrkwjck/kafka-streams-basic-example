package example.model;

import lombok.ToString;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ToString
@Value
public class Shipment {

    @NotNull
    String id;

    @NotNull
    @Valid
    ShipmentProduct product;

    @NotNull
    @Valid
    Recipient recipient;

}
