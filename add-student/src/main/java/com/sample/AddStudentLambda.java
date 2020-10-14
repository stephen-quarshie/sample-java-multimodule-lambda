package com.sample;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.UUID;

public class AddStudentLambda {
    private static final Logger log = LoggerFactory.getLogger(AddStudentLambda.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamoDbEnhancedClient enhancedClient;
    private static final TableSchema<Student> CUSTOMER_TABLE_SCHEMA = TableSchema.fromBean(Student.class);

    public AddStudentLambda() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://local-dynamodb:8000"))
                .build();

        enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
    }

    public APIGatewayProxyResponseEvent handler(APIGatewayProxyRequestEvent request, Context context) {
        Student student;
        try {
            /*
            Get student from request body and use Jackson object mapper to marshall into
            a Student object and set a unique id for the Student
             */
            student = objectMapper.readValue(request.getBody(), Student.class);
            student.setId(UUID.randomUUID().toString());

            /*
            Get a reference to the Students table and save the student the table.
             */
            DynamoDbTable<Student> table = enhancedClient.table(Student.TABLE_NAME, CUSTOMER_TABLE_SCHEMA);
            table.putItem(student);

        } catch (JsonProcessingException jpe) {
            log.info(jpe.getMessage(), jpe);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400) // BAD REQUEST
                    .withBody(jpe.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500) // INTERNAL SERVER ERROR
                    .withBody(context.getAwsRequestId() + ":" + e.getMessage());
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200) // OK
                .withBody("StudentID: " + student.getId());

    }
}
