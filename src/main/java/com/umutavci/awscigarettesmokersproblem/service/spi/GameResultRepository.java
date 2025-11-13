package com.umutavci.awscigarettesmokersproblem.service.spi;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;

public interface GameResultRepository {
    void saveGameStart(Table table);
    void saveGameResult(Table table, User winner);
}
