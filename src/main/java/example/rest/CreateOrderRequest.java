package example.rest;

import example.model.Item;
import example.model.Recipient;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Value
class CreateOrderRequest{

    @NotNull
    @Valid
    Item item;

    @NotNull
    @Valid
    Recipient recipient;

}
