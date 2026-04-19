package model;

public class Product {
    private int id;
    private int farmerId;
    private String name;
    private int quantity;
    private double price;

    public Product(int id, int farmerId, String name, int quantity, double price) {
        this.id = id;
        this.farmerId = farmerId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public int getFarmerId() { return farmerId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}