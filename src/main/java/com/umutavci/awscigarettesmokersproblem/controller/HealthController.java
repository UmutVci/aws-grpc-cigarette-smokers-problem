package com.umutavci.awscigarettesmokersproblem.controller;

import com.umutavci.awscigarettesmokersproblem.service.TableManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final TableManager manager;

    public HealthController(TableManager manager) {
        this.manager = manager;
    }
    @GetMapping("/health")
    public String health() {
        return "mal erkin";
    }
    @GetMapping("/tables/open")
    public Object openTables() {
        return manager.listOpenTables().stream().map(t -> t.getTableName()).toList();
    }
}

