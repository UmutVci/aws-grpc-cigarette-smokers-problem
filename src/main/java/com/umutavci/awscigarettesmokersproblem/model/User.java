package com.umutavci.awscigarettesmokersproblem.model;

import lombok.Data;

@Data
public class User {
    private final String name;
    private Ingredient own;
    private String currentTableId;

    public User(String name) {
        this.name = name;
    }
    public User(String name, Ingredient ingredient){
        this.name = name;
        this.own = ingredient;
    }

    public void clearIngredients() {
        own = Ingredient.UNKNOWN;
    }

}