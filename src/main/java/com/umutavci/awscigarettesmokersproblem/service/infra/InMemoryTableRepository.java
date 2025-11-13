package com.umutavci.awscigarettesmokersproblem.service.infra;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;
import com.umutavci.awscigarettesmokersproblem.service.spi.TableRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryTableRepository implements TableRepository {

    private final Map<String, Table> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Table> findById(String tableId) {
        return Optional.ofNullable(store.get(tableId));
    }

    @Override
    public List<Table> listOpenTables() {
        return store.values().stream()
                .filter(t -> !t.isStarted() && !t.isBooked())
                .collect(Collectors.toList());
    }

    @Override
    public Table save(Table table) {
        store.put(table.getTableName(), table);
        return table;
    }

    @Override
    public void delete(String tableId) {
        store.remove(tableId);
    }

    @Override
    public List<Table> allTables() {
        return store.values().stream().sorted(byBookingAndPlayerCount()).toList();
    }

    @Override
    public List<String> getAllUsersOnTable(String tableId) {
        for(Table t : store.values()){
            if(t.getTableName().equals(tableId)){
                return t.getSmokers().stream().map(User::getName).toList();
            }
        }
        return List.of();
    }

    private static Comparator<Table> byBookingAndPlayerCount() {
        return Comparator
                .comparing(Table::isBooked)
                .thenComparing(t -> t.getSmokers().size());
    }
}
