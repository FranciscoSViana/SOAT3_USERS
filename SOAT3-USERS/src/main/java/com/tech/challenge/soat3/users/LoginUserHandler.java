package com.tech.challenge.soat3.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tech.challenge.soat3.users.service.CognitoUserService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.HashMap;
import java.util.Map;

public class LoginUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CognitoUserService cognitoUserService;

    private final String appClientId;

    private final String appClientSecret;

    public LoginUserHandler() {
        this.cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));
        this.appClientId = System.getenv("MY_COGNITO_POOL_APP_CLIENT_ID");
        this.appClientSecret = System.getenv("MY_COGNITO_POOL_APP_CLIENT_SECRET");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        LambdaLogger logger = context.getLogger();;

        try {
            JsonObject loginDetails = JsonParser.parseString(input.getBody()).getAsJsonObject();
            JsonObject loginResult = cognitoUserService.userLogin(loginDetails, appClientId, appClientSecret);

            response.withBody(new Gson().toJson(loginResult, JsonObject.class));
            response.withStatusCode(200);
            logger.log("Deu certo com o Login");
        } catch (AwsServiceException e) {
            logger.log(e.awsErrorDetails().errorMessage());

            ErrorResponse errorResponse = new ErrorResponse(e.awsErrorDetails().errorMessage());
            String errorResponseJsonString = new Gson().toJson(errorResponse, ErrorResponse.class);

            response.withBody(errorResponseJsonString);
            response.withStatusCode(500);
        } catch (Exception e) {
            logger.log(e.getMessage());

            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            //String errorResponseJsonString = new Gson().toJson(errorResponse, ErrorResponse.class);
            String errorResponseJsonString = new GsonBuilder().serializeNulls().create().toJson(errorResponse, ErrorResponse.class);

            response.withBody(errorResponseJsonString);
            response.withStatusCode(500);
        }



        return response;
    }
}
