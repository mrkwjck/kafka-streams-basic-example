package example.model;

import lombok.Value;

import javax.validation.constraints.NotEmpty;


@Value
public class Recipient {

    @NotEmpty
    String firstName;

    @NotEmpty
    String lastName;

}
