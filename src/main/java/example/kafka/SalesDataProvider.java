package example.kafka;

import example.model.ShipmentProduct;

import java.util.List;

public interface SalesDataProvider {

    List<ShipmentProduct> getProductsSales();
}

