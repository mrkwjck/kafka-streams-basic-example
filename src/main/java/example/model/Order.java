package example.model;

import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ToString
@Value
public class Order {

    @NotEmpty
    String id;

    @NotNull
    Item item;

    @NotNull
    Recipient recipient;

}
