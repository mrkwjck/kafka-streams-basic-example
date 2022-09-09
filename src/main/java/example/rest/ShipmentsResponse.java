package example.rest;

import example.model.Shipment;
import lombok.Value;

import java.util.List;

@Value
class ShipmentsResponse {

    List<Shipment> shipments;

}
