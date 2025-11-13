package com.umutavci.awscigarettesmokersproblem.adapter.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.service.spi.TableRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("prod")
public class RedisTableRepository implements TableRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> ops;
    private final ObjectMapper mapper;
    private static final String PREFIX = "table:";

    public RedisTableRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.redisTemplate = redisTemplate;
        this.ops = redisTemplate.opsForValue();
        this.mapper = mapper;
    }

    @Override
    public Optional<Table> findById(String tableId) {
        String json = ops.get(PREFIX + tableId);
        if (json == null) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(json, Table.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse table JSON", e);
        }
    }

    public List<Table> listOpenTables() {
        List<Table> result = new ArrayList<>();
        try (Cursor<byte[]> cursor = (Cursor<byte[]>) redisTemplate
                .getConnectionFactory()
                .getConnection()
                .scan(ScanOptions.scanOptions().match(PREFIX + "*").count(100).build())) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                String json = ops.get(key);
                if (json == null) continue;
                Table t = mapper.readValue(json, Table.class);
                if (!t.isBooked() && !t.isStarted()) result.add(t);
            }
        } catch (Exception e) {
            throw new RuntimeException("Redis SCAN failed", e);
        }
        return result;
    }


    @Override
    public Table save(Table table) {
        try {
            String key = PREFIX + table.getTableName();
            String json = mapper.writeValueAsString(table);
            ops.set(key, json, Duration.ofMinutes(10)); // TTL 10 dk
            return table;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize table", e);
        }
    }

    @Override
    public void delete(String tableId) {
        redisTemplate.delete(PREFIX + tableId);
    }

    @Override
    public List<Table> allTables() {
        // TODO : DO
        return List.of();
    }

    @Override
    public List<String> getAllUsersOnTable(String tableId) {
        // TODO : DO
        return List.of();
    }
}
