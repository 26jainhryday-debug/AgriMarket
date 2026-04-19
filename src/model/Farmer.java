package model;

public class Farmer extends Person {
    private int id;
    private String location;

    public Farmer(int id, String name, int age, String location) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.location = location;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getLocation() { return location; }

    @Override
    public void display() {
        System.out.println(id + " " + name + " " + age + " " + location);
    }
}