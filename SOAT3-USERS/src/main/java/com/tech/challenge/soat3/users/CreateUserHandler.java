package com.tech.challenge.soat3.users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tech.challenge.soat3.users.service.CognitoUserService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

/**
 * Handler for requests to Lambda function.
 */
public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final CognitoUserService cognitoUserService;

    private final String appClientId;

    private final String appClientSecret;

    public CreateUserHandler() {
        this.cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));
        this.appClientId = System.getenv("MY_COGNITO_POOL_APP_CLIENT_ID");
        this.appClientSecret = System.getenv("MY_COGNITO_POOL_APP_CLIENT_SECRET");
    }
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        String requestBody = input.getBody();
        LambdaLogger logger = context.getLogger();
        logger.log("Original json body: " + requestBody);

        JsonObject userDetails = JsonParser.parseString(requestBody).getAsJsonObject();

        try {
            JsonObject createUserResult = cognitoUserService.createUser(userDetails, appClientId, appClientSecret);
            response.withStatusCode(200);
            response.withBody(new Gson().toJson(createUserResult, JsonObject.class));
        } catch (AwsServiceException e) {
            logger.log(e.awsErrorDetails().errorMessage());
            response.withStatusCode(500);
            response.withBody(e.awsErrorDetails().errorMessage());
        }

        return response;
    }
}
