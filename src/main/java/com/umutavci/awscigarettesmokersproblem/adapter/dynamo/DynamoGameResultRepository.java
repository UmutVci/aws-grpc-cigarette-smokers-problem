package com.umutavci.awscigarettesmokersproblem.adapter.dynamo;

import com.umutavci.awscigarettesmokersproblem.model.Table;
import com.umutavci.awscigarettesmokersproblem.model.User;
import com.umutavci.awscigarettesmokersproblem.service.spi.GameResultRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Repository
@Profile("prod")
public class DynamoGameResultRepository implements GameResultRepository {

    private final DynamoDbClient dynamo;

    public DynamoGameResultRepository() {
        this.dynamo = DynamoDbClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
    }

    @Override
    public void saveGameStart(Table table) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("GameId", AttributeValue.builder().s(table.getTableName()).build());
        item.put("Status", AttributeValue.builder().s("STARTED").build());
        item.put("StartedAt", AttributeValue.builder().s(Instant.now().toString()).build());
        item.put("Players", AttributeValue.builder()
                .ss(table.getSmokers().stream().map(User::getName).toList())
                .build());

        dynamo.putItem(PutItemRequest.builder()
                .tableName("Games")
                .item(item)
                .build());
    }

    @Override
    public void saveGameResult(Table table, User winner) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("GameId", AttributeValue.builder().s(table.getTableName()).build());
        item.put("Status", AttributeValue.builder().s("FINISHED").build());
        item.put("Winner", AttributeValue.builder().s(winner.getName()).build());
        item.put("FinishedAt", AttributeValue.builder().s(Instant.now().toString()).build());

        dynamo.putItem(PutItemRequest.builder()
                .tableName("Games")
                .item(item)
                .build());
    }
}
