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

    // her masa için concurrency kontrolü
    private final Map<String, ReentrantLock> tableLocks = new ConcurrentHashMap<>();

    /** Redis'ten açık masa bulur, yoksa yeni oluşturur. */
    public Table findOrCreateAvailableTable() {
        List<Table> open = tableRepo.listOpenTables();
        if (!open.isEmpty()) {
            return open.get(0);
        }
        return createNewTable();
    }

    /** Yeni masa oluşturur ve Redis’e yazar. */
    public Table createNewTable() {
        String id = "table-" + UUID.randomUUID();
        Table table = new Table(id);
        tableRepo.save(table);
        return table;
    }

    /** Kullanıcıyı masaya ekler. Masa dolarsa oyunu başlatır ve DynamoDB’ye başlangıç kaydı yazar. */
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

    /** Oyun bittiğinde temizlik yapar, sonucu Dynamo’ya kaydeder ve Redis’ten masayı siler. */
    public void onGameEnded(String tableId, User winner) {
        ReentrantLock lock = tableLocks.computeIfAbsent(tableId, k -> new ReentrantLock());
        lock.lock();
        try {
            Table table = tableRepo.findById(tableId)
                    .orElseThrow(() -> new NoSuchElementException("Table not found: " + tableId));

            // 🟦 Sonucu DynamoDB’ye kaydet
            gameResultRepo.saveGameResult(table, winner);

            // 🟥 Redis'ten masayı kaldır
            tableRepo.delete(tableId);

            // Lock temizliği
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