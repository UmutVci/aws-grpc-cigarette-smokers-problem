package com.umutavci.awscigarettesmokersproblem.service.spi;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;

import java.util.List;
import java.util.Optional;

public interface TableRepository {
    Optional<Table> findById(String tableId);
    List<Table> listOpenTables();
    Table save(Table table);
    void delete(String tableId);
    List<Table> allTables();
    List<String> getAllUsersOnTable(String tableId);
}
