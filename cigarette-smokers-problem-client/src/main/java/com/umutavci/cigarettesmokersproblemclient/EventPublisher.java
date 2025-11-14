package com.umutavci.cigarettesmokersproblemclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
public class EventPublisher {

    private final GameStreamClient gameStreamClient;

    public EventPublisher(GameStreamClient gameStreamClient) {
        this.gameStreamClient = gameStreamClient;
    }

    @GetMapping(value = "/api/game/events/{tableId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGameEvents(@PathVariable String tableId) {
        log.info("🔌 New SSE connection for table {}", tableId);

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> log.info("✅ SSE complete for table {}", tableId));
        emitter.onTimeout(() -> {
            log.warn("⏳ SSE timeout for table {}", tableId);
            emitter.complete();
        });
        emitter.onError(e -> {
            log.error("❌ SSE error for table {}: {}", tableId, e.getMessage());
            emitter.complete();
        });

        gameStreamClient.subscribeToGameEvents(tableId, response -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("game-server")
                        .data(response.getMessage()));

                log.info("📡 GRPCSSE Sent event to {}: {}", tableId, response.getMessage());

            } catch (IOException e) {
                log.error("❌ Error sending SSE data, client disconnected: {}", tableId);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}