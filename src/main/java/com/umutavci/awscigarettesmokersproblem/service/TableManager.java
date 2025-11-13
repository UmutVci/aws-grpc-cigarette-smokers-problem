package com.umutavci.awscigarettesmokersproblem.service;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;
import com.umutavci.awscigarettesmokersproblem.service.spi.GameResultRepository;
import com.umutavci.awscigarettesmokersproblem.service.spi.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class TableManager {

    private final TableRepository tableRepo;
    private final GameResultRepository gameResultRepo;

    private final Map<String, ReentrantLock> tableLocks = new ConcurrentHashMap<>();

    public Table findOrCreateAvailableTable() {
        List<Table> open = tableRepo.listOpenTables();
        if (!open.isEmpty()) {
            return open.get(0);
        }
        return createNewTable();
    }

    public Table createNewTable() {
        String id = "table-" + UUID.randomUUID();
        Table table = new Table(id);
        tableRepo.save(table);
        return table;
    }

    public String addUserToTable(User user, String tableId) {
        Table table = tableRepo.findById(tableId)
                .orElseThrow(() -> new NoSuchElementException("Table not found: " + tableId));

        ReentrantLock lock = tableLocks.computeIfAbsent(tableId, k -> new ReentrantLock());
        lock.lock();
        try {
            String res = table.addUser(user);
            tableRepo.save(table);
            if (table.isStarted()) {
                gameResultRepo.saveGameStart(table);
            }
            return res;
        } finally {
            lock.unlock();
        }
    }

    public void onGameEnded(String tableId, User winner) {
        ReentrantLock lock = tableLocks.computeIfAbsent(tableId, k -> new ReentrantLock());
        lock.lock();
        try {
            Table table = tableRepo.findById(tableId)
                    .orElseThrow(() -> new NoSuchElementException("Table not found: " + tableId));

            gameResultRepo.saveGameResult(table, winner);
            tableRepo.delete(tableId);
            tableLocks.remove(tableId);

        } finally {
            lock.unlock();
        }
    }

    public Optional<Table> getTable(String tableId) {
        return tableRepo.findById(tableId);
    }

    public List<Table> listOpenTables() {
        return tableRepo.listOpenTables();
    }

}