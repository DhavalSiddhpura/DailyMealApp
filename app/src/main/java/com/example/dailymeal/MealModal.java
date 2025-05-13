package com.example.dailymeal;

import java.util.Objects;

public class MealModal {
    private String name, description, mealType, imgurl;
    private int price;
    private boolean available;
    private int quantity; // ✅ New field added

    // Required empty constructor for Firestore
    public MealModal() {
        this.quantity = 1; // Default quantity is 1
    }

    public MealModal(String name, String description, String mealType, String imgurl, int price, boolean available) {
        this.name = name;
        this.description = description;
        this.mealType = mealType;
        this.imgurl = imgurl;
        this.price = price;
        this.available = available;
        this.quantity = 1; // Default quantity is 1
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getMealType() { return mealType; }
    public String getImgurl() { return imgurl; }
    public int getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public int getQuantity() { return quantity; } // ✅ Getter for quantity

    // Setter
    public void setQuantity(int quantity) { // ✅ Setter for quantity
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MealModal meal = (MealModal) obj;
        return name.equals(meal.name) && mealType.equals(meal.mealType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, mealType);
    }
}
