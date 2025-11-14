package com.umutavci.cigarettesmokersproblemclient;

import com.umutavci.grpc.GameEventRequest;
import com.umutavci.grpc.GameEventResponse;
import com.umutavci.grpc.GameServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Service
public class GameStreamClient {

    @GrpcClient("game-server")
    public GameServiceGrpc.GameServiceStub asyncStub;

    // for active streams
    private final Map<String, StreamObserver<GameEventRequest>> activeStreams = new ConcurrentHashMap<>();

    public void subscribeToGameEvents(String tableId, Consumer<GameEventResponse> onEvent) {
        GameEventRequest request = GameEventRequest.newBuilder()
                .setTableId(tableId)
                .build();

        asyncStub.streamGameEvents(request, new StreamObserver<>() {
            @Override
            public void onNext(GameEventResponse response) {
                log.info("Received event: {}", response.getMessage());
                onEvent.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                log.error("❌ CRITICAL STREAM ERROR - gRPC Status: {} | Message: {}",
                        io.grpc.Status.fromThrowable(t), t.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("Stream completed for table {}", tableId);
            }
        });
    }
}
