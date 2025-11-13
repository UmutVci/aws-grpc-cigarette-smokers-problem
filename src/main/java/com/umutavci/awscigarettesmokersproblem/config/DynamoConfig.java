package com.umutavci.awscigarettesmokersproblem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

/*
🔐 AWS erişim bilgileri otomatik olarak alınır (DefaultCredentialsProvider):

Environment variable (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)

~/.aws/credentials

EC2/ECS IAM Role
 */
