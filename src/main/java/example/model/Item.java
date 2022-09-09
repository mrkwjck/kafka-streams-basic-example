package example.model;

import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
public class Item {

    @NotEmpty
    String id;

    int quantity;

}
