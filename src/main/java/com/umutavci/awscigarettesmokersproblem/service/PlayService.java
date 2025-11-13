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

    /** Kullanıcı oyuna girmek istiyor → open table bul veya yeni oluştur, ekle. */
    public String userWantToPlay(String username) {
        User user = new User(username);
        Table table = tableManager.findOrCreateAvailableTable();
        return tableManager.addUserToTable(user, table.getTableName());
    }

    /** Kullanıcı istediği masaya geçmek istiyor. */
    public String joinSpecificTable(String username, String tableId) {
        User user = new User(username);
        return tableManager.addUserToTable(user, tableId);
    }

    /** Kullanıcı masadan ayrılıyor. */
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
