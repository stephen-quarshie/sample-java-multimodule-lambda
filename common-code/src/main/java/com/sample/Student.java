package com.sample;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@ToString
@DynamoDbBean
public class Student {
    public static final String TABLE_NAME = "Students";
    private String id;
    private String firstname;
    private String lastname;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
