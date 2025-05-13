package com.example.dailymeal;

import java.util.ArrayList;
import java.util.List;

public class SelectedMealManager {
    private static SelectedMealManager instance;
    private final List<MealModal> selectedMeals = new ArrayList<>();

    private SelectedMealManager() {}

    public static SelectedMealManager getInstance() {
        if (instance == null) {
            instance = new SelectedMealManager();
        }
        return instance;
    }

    public void addMeal(MealModal meal) {
        selectedMeals.add(meal);
    }

    public List<MealModal> getSelectedMeals() {
        return selectedMeals;
    }

    public void clearMeals() {
        selectedMeals.clear();
    }
    public void removeMeal(MealModal meal) {
        selectedMeals.remove(meal); // Make sure MealModal has equals() and hashCode() overridden properly
    }

    private int currentMealTotal, handlingFee, deliveryFee;

    public void setCurrentMealTotal(int mealTotal) { this.currentMealTotal = mealTotal; }
    public void setHandlingFee(int handlingFee) { this.handlingFee = handlingFee; }
    public void setDeliveryFee(int deliveryFee) { this.deliveryFee = deliveryFee; }

    public int getCurrentMealTotal() { return currentMealTotal; }
    public int getHandlingFee() { return handlingFee; }
    public int getDeliveryFee() { return deliveryFee; }

}
