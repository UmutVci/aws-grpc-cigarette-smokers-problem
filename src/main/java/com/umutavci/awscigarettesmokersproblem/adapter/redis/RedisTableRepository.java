package com.umutavci.awscigarettesmokersproblem.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.service.spi.TableRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Profile("prod")
public class RedisTableRepository implements TableRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> ops;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String PREFIX = "table:";

    public RedisTableRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.ops = redisTemplate.opsForValue();
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

    @Override
    public List<Table> listOpenTables() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys == null || keys.isEmpty()) return List.of();

        return keys.stream()
                .map(k -> ops.get(k))
                .filter(json -> json != null && !json.isEmpty())
                .map(json -> {
                    try {
                        return mapper.readValue(json, Table.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .filter(t -> t != null && !t.isBooked() && !t.isStarted())
                .collect(Collectors.toList());
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
}
