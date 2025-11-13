package com.umutavci.awscigarettesmokersproblem.grpc;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;
import com.umutavci.grpc.*;
import io.grpc.stub.StreamObserver;

import com.umutavci.awscigarettesmokersproblem.service.PlayService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
@RequiredArgsConstructor
@Getter
@Setter
public class GameGrpcService extends GameServiceGrpc.GameServiceImplBase {

    private final PlayService playService;
    private final Map<String, List<StreamObserver<GameEventResponse>>> eventStreams = new ConcurrentHashMap<>();


    @Override
    public void joinGame(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
        String result = playService.userWantToPlay(request.getUsername());
        String tableId = extractTableIdFromResult(result);
        String eventMessage = request.getUsername() + " joined the game with that ingredient ";
        // Bu tableId'yi dinleyen tüm stream observer'larına mesajı gönderiyoruz.
        playService.getTableRepo().findById(tableId).ifPresent(table -> {
            table.setEventCallback(msg -> broadcastEvent(tableId, msg));
        });

        JoinResponse response = JoinResponse.newBuilder()
                .setMessage(result)
                .setTableId(tableId)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        broadcastEvent(tableId, eventMessage);
    }

    @Override
    public void joinSpecificTable(JoinSpecificRequest request, StreamObserver<JoinResponse> responseObserver) {
        String result = playService.joinSpecificTable(request.getUsername(), request.getTableId());
        JoinResponse response = JoinResponse.newBuilder().setMessage(result).build();

        playService.getTableRepo().findById(request.getTableId()).ifPresent(table -> {
            table.setEventCallback(msg -> broadcastEvent(request.getTableId(), msg));
        });

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        broadcastEvent(request.getTableId(), request.getUsername() + " joined the game");
    }

    @Override
    public void leaveTable(LeaveRequest request, StreamObserver<LeaveResponse> responseObserver) {
        String result = playService.leaveTable(request.getUsername(), request.getTableId());
        LeaveResponse response = LeaveResponse.newBuilder().setMessage(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        broadcastEvent(request.getTableId(), request.getUsername() + " left the game");
    }

    @Override
    public void getTableStatus(TableRequest request, StreamObserver<TableResponse> responseObserver) {
        Table table = playService.getTableRepo()
                .findById(request.getTableId())
                .orElse(null);

        TableResponse.Builder builder = TableResponse.newBuilder().setTableId(request.getTableId());
        if (table != null) {
            builder.setStarted(table.isStarted())
                    .setBooked(table.isBooked())
                    .addAllSmokers(table.getSmokers().stream().map(User::getName).toList())
                    .setPlayerCount(table.getSmokers().size());
        } else {
            builder.setStarted(false).setBooked(false).setPlayerCount(0);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void allTables(Empty request, StreamObserver<AllTablesResponse> responseObserver) {
        try {
            List<Table> domainTables = playService.getTableRepo().allTables();

            List<TableResponse> grpcTables = domainTables.stream()
                    .map(t -> TableResponse.newBuilder()
                            .setTableId(t.getTableName())
                            .setBooked(t.isBooked())
                            .setStarted(t.isStarted())
                            .setPlayerCount(t.getSmokers().size())
                            .addAllSmokers(
                                    t.getSmokers().stream()
                                            .map(User::getName)
                                            .toList()
                            )
                            .build()
                    )
                    .toList();

            AllTablesResponse response = AllTablesResponse.newBuilder()
                    .addAllTables(grpcTables)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAllUsersOnTable(TableRequest request, StreamObserver<UsersOnTableResponse> responseObserver) {
        try {
            String tableId = request.getTableId();

            Table table = playService.getTableRepo().findById(tableId).orElseThrow();

            List<JoinRequest> userList = table.getSmokers().stream()
                    .map(u -> JoinRequest.newBuilder()
                            .setUsername(u.getName())
                            .build())
                    .toList();

            UsersOnTableResponse response = UsersOnTableResponse.newBuilder()
                    .addAllUsers(userList)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
    // streamGameEvents methodunu, game event'leri frontend'e iletmek için kullanacağız
    @Override
    public void streamGameEvents(GameEventRequest request, StreamObserver<GameEventResponse> responseObserver) {
        String tableId = request.getTableId();

        // Yeni subscriber ekliyoruz
        eventStreams.computeIfAbsent(tableId, k -> {
            System.out.println("🎧 New subscriber for table " + tableId); // Subscriber ekleniyor
            return new ArrayList<>(); // Yeni bir ArrayList döner ve subscriber eklenir
        }).add(responseObserver);

        System.out.println("🎧 Total subscribers for table " + tableId + ": " + eventStreams.get(tableId).size());
    }

    void broadcastEvent(String tableId, String message) {
        GameEventResponse response = GameEventResponse.newBuilder()
                .setTableId(tableId)
                .setMessage(message)
                .build();

        List<StreamObserver<GameEventResponse>> observers = eventStreams.get(tableId);
        if (observers != null) {
            observers.removeIf(obs -> {
                try {
                    System.out.println("🎮 Sending event to subscriber for table " + tableId);
                    obs.onNext(response);
                    return false;
                } catch (Exception e) {
                    System.err.println("❌ Observer için yayınlama hatası: " + e.getMessage());
                    obs.onError(e);
                    return true;
                }
            });
        } else {
            System.out.println("⚠️ No subscribers for table " + tableId);
        }
    }

    private String extractTableIdFromResult(String result) {
        if (result != null && result.contains("table-")) {
            return result.substring(result.indexOf("table-")).trim();
        }
        return "";
    }
}
