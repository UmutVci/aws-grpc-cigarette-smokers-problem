package com.umutavci.awscigarettesmokersproblem.service.infra;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;
import com.umutavci.awscigarettesmokersproblem.service.spi.GameResultRepository;
import org.springframework.stereotype.Repository;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class InMemoryGameResultRepository implements GameResultRepository {

    @Override
    public void saveGameStart(Table table) {
        log.info("Game START tableId={} players={}", table.getTableName(), table.getSmokers().size());
    }

    @Override
    public void saveGameResult(Table table, User winner) {
        log.info("Game END tableId={} winner={}", table.getTableName(), winner.getName());
    }
}