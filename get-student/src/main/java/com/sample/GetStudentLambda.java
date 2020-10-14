package com.sample;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

public class GetStudentLambda {
    private static final Logger log = LoggerFactory.getLogger(GetStudentLambda.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamoDbEnhancedClient enhancedClient;
    private static final TableSchema<Student> CUSTOMER_TABLE_SCHEMA = TableSchema.fromBean(Student.class);

    public GetStudentLambda() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://local-dynamodb:8000"))
                .build();

        enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
    }

    public APIGatewayProxyResponseEvent handler(APIGatewayProxyRequestEvent request, Context context) {
        Student student = null;

        try {
            String studentId = request.getPathParameters().get("studentId");
            //if (studentId == null) throw new IllegalArgumentException("A student id is required in the url path");

            DynamoDbTable<Student> table = enhancedClient.table(Student.TABLE_NAME, CUSTOMER_TABLE_SCHEMA);
            Key key = Key.builder().partitionValue(studentId).build();
            student = table.getItem(key);

            return student == null
                    ? new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("Student not found")
                    : new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(objectMapper.writeValueAsString(student));

        } catch (IllegalArgumentException iae) {
            log.info(iae.getMessage(), iae);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400) // BAD REQUEST
                    .withBody(iae.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500) // INTERNAL SERVER ERROR
                    .withBody(context.getAwsRequestId() + ":" + e.getMessage());
        }
    }
}
