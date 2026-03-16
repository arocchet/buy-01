package com.letsplay.orderservice.model;

public class CartItem {

    private String productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private String sellerId;

    public CartItem() {}

    public CartItem(String productId, String productName, Integer quantity, Double unitPrice, String sellerId) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.sellerId = sellerId;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public Double getSubtotal() {
        return unitPrice * quantity;
    }
}
