package com.umutavci.cigarettesmokersproblemclient;

import com.umutavci.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/game")
public class GameRestController {

    @GrpcClient("game-server")
    private GameServiceGrpc.GameServiceBlockingStub gameStub;

    @PostMapping("/join")
    public ResponseEntity<JoinResponseDto> join(@RequestBody JoinRequestDto dto) {
        var resp = gameStub.joinGame(
                JoinRequest.newBuilder().setUsername(dto.username()).build()
        );

        String message = resp.getMessage();
        String tableId = extractTableId(message);

        return ResponseEntity.ok(new JoinResponseDto(message, tableId));
    }

    @PostMapping("/join-specific")
    public ResponseEntity<JoinResponseDto> joinSpecific(@RequestBody JoinSpecificRequestDto dto) {
        var resp = gameStub.joinSpecificTable(
                JoinSpecificRequest.newBuilder()
                        .setUsername(dto.username())
                        .setTableId(dto.tableId())
                        .build()
        );
        String message = resp.getMessage();
        String tableId = extractTableId(message);
        return ResponseEntity.ok(new JoinResponseDto(message, tableId));
    }

    @PostMapping("/leave")
    public ResponseEntity<LeaveResponseDto> leave(@RequestBody LeaveRequestDto dto) {
        var resp = gameStub.leaveTable(
                LeaveRequest.newBuilder()
                        .setUsername(dto.username())
                        .setTableId(dto.tableId())
                        .build()
        );
        return ResponseEntity.ok(new LeaveResponseDto(resp.getMessage()));
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<TableBigResponseDto> getTable(@PathVariable String tableId) {
        TableResponse resp = gameStub.getTableStatus(
                TableRequest.newBuilder().setTableId(tableId).build()
        );
        return ResponseEntity.ok(
                new TableBigResponseDto(
                        resp.getTableId(),
                        resp.getStarted(),
                        resp.getBooked(),
                        resp.getPlayerCount(),
                        resp.getSmokersList()
                )
        );
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TableResponseDto>> getAllTables() {
        AllTablesResponse resp = gameStub.allTables(Empty.newBuilder().build());

        List<TableResponseDto> tables = resp.getTablesList().stream()
                .map(t -> new TableResponseDto(
                        t.getTableId(),
                        t.getStarted(),
                        t.getBooked(),
                        t.getPlayerCount()
                ))
                .toList();
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/table/{tableId}/users")
    public ResponseEntity<List<String>> getAllUsersOnTable(@PathVariable String tableId){
        UsersOnTableResponse resp = gameStub
                .getAllUsersOnTable(TableRequest.newBuilder().setTableId(tableId).build());
        List<String> users = resp.getUsersList()
                .stream()
                .map(JoinRequest::getUsername)
                .toList();
        return ResponseEntity.ok(users);
    }



    private String extractTableId(String message) {
        if (message != null && message.contains("table-")) {
            return message.substring(message.indexOf("table-")).trim();
        }
        return "";
    }

    public record JoinRequestDto(String username) {}
    public record JoinResponseDto(String message, String tableId) {}

    // Join a specific table
    public record JoinSpecificRequestDto(String username, String tableId) {}
    public record JoinSpecificResponseDto(String message) {}

    // Leave a table
    public record LeaveRequestDto(String username, String tableId) {}
    public record LeaveResponseDto(String message) {}

    // Get table status
    public record TableRequestDto(String tableId) {}
    public record TableResponseDto(String tableId, boolean started, boolean booked, int playerCount) {}
    public record TableBigResponseDto(String tableId, boolean started, boolean booked, int playerCount, List<String> players) {}
}

