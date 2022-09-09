package example.model;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Valid
@Value
public class ShipmentProduct {

    @NotNull
    String id;

    @NotNull
    String name;

    int orderedQuantity;

}
