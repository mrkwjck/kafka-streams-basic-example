package example.model;

import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@ToString
@Value
public class StockProduct {

    @NotEmpty
    String id;

    @NotEmpty
    String name;

}
