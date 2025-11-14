package com.umutavci.awscigarettesmokersproblem.service;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;
import com.umutavci.awscigarettesmokersproblem.service.spi.TableRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Getter
@RequiredArgsConstructor
public class PlayService {

    private final TableManager tableManager;
    private final TableRepository tableRepo;

    /** The user wants to join a game → find an open table or create a new one, then add them. */

    public String userWantToPlay(String username) {
        User user = new User(username);
        Table table = tableManager.findOrCreateAvailableTable();
        return tableManager.addUserToTable(user, table.getTableName());
    }

    /** The user wants to move to the table they choose. */

    public String joinSpecificTable(String username, String tableId) {
        User user = new User(username);
        return tableManager.addUserToTable(user, tableId);
    }

    /** The user is leaving the table. */

    public String leaveTable(String username, String tableId) {
        Table table = tableManager.getTable(tableId)
                .orElseThrow(() -> new NoSuchElementException("Table not found: " + tableId));

        synchronized (table) {
            boolean removed = table.getSmokers().removeIf(u -> u.getName().equals(username));
            if (removed) {
                if (table.getSmokers().size() < 3) {
                    table.setBooked(false);
                    table.setStarted(false);
                }
                tableRepo.save(table);
                return "User " + username + " left table " + tableId;
            } else {
                return "User not found at table";
            }
        }
    }
}
