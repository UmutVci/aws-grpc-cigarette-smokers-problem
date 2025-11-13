package com.umutavci.awscigarettesmokersproblem.model;

import lombok.Data;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Data
public class Table {

    private final String tableName;
    private final List<User> smokers = Collections.synchronizedList(new ArrayList<>());
    private final List<Ingredient> allIngredients = List.of(Ingredient.PAPER, Ingredient.MATCHES, Ingredient.TOBACCO);
    private final List<Ingredient> tableIngredients = new ArrayList<>(2);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Random random = new Random();

    private boolean isStarted = false;
    private boolean isBooked = false;

    private Consumer<String> eventCallback;

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public synchronized String addUser(User user) {
        if (isStarted) return "Error: Table already started";
        if (isBooked) return "Error: Table already booked";
        if (smokers.stream().anyMatch(s -> s.getName().equals(user.getName())))
            return "Error: User already sitting at this table";

        Ingredient assignedIngredient = allIngredients.get(smokers.size() % allIngredients.size());
        User assigned = new User(user.getName(), assignedIngredient);
        smokers.add(assigned);

        raise(assigned.getName() + " joined " + tableName + " with " + assigned.getOwn());


        if (smokers.size() == 3) {
            isBooked = true;
            isStarted = true;
            executor.submit(this::startGameLoop);
        }

        return assigned.getName() + "+ \" with \" + " + assigned.getOwn() + " + joined " + tableName;
    }

    private void startGameLoop() {
        try {
            while (isStarted) {
                // Dealer puts two ingreadients
                putIngredient();
                System.out.println("Dealer puts on table: " + tableIngredients);
                raise("Dealer puts on table: " + tableIngredients);

                // Find the winner
                User winner = findWinner();
                if (winner != null) {
                    takeIngredient(winner);
                } else {
                    raise("No smoker can act this round at " + tableName);
                }

                Thread.sleep(3000);
                tableIngredients.clear();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void putIngredient() {
        tableIngredients.clear();
        while (tableIngredients.size() < 2) {
            Ingredient next = allIngredients.get(random.nextInt(allIngredients.size()));
            if (!tableIngredients.contains(next)) {
                tableIngredients.add(next);
            }
        }
    }

    private User findWinner() {
        Set<Ingredient> missing = new HashSet<>(allIngredients);
        missing.removeAll(tableIngredients);
        if (missing.size() != 1) return null;
        Ingredient needed = missing.iterator().next();

        return smokers.stream()
                .filter(s -> s.getOwn() == needed)
                .findFirst()
                .orElse(null);
    }

    // The winner is smoking
    private void takeIngredient(User winner) {
        raise("Smoking this round: " + winner.getName());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private void raise(String msg) {
        if (eventCallback != null) {
            eventCallback.accept(tableName + ":" + msg);
        }

    }
}