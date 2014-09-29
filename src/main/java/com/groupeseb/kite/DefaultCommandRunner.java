package com.groupeseb.kite;

import com.groupeseb.kite.check.Check;
import com.groupeseb.kite.check.DefaultCheckRunner;
import com.groupeseb.kite.check.ICheckRunner;
import com.groupeseb.kite.function.Function;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

import static com.jayway.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;

@Slf4j
@NoArgsConstructor
@Component
public class DefaultCommandRunner implements ICommandRunner {
    private static final String JSON_UTF8 = ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), "UTF-8").toString();
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private final ICheckRunner checkRunner = new DefaultCheckRunner();

    @Override
    public void execute(Command command, CreationLog creationLog, ApplicationContext context) throws Exception {
        creationLog.setAvailableFunctions(context.getBeansOfType(Function.class).values());

        if (command.getDescription() != null) {
            log.info(command.getDescription() + "...");
        }

        if (command.getDisabled()) {
            log.warn("Disabled command : Skipped.");
            return;
        }

        if (command.getWait() > 0) {
            log.info("Waiting for " + command.getWait() + "ms...");
            Thread.sleep(command.getWait());
        }

        switch (command.getVerb().toUpperCase()) {
            case (POST):
                post(command, creationLog, context);
                break;
            case (GET):
                get(command, creationLog, context);
                break;
            case (PUT):
                put(command, creationLog, context);
                break;
            case (DELETE):
                delete(command, creationLog, context);
                break;
        }

        log.info("[" + command.getName() + "] OK");
    }

    void post(Command command, CreationLog creationLog, ApplicationContext context) throws ParseException {
        log.info("[" + command.getName() + "] POST " + command.getProcessedURI(creationLog) + " (expecting " + command.getExpectedStatus() + ")");

        if (command.getDebug()) {
            log.info("[" + command.getName() + "] " + command.getProcessedBody(creationLog));
        }

        Response postResponse = given()
                .contentType(JSON_UTF8).headers(command.getProcessedHeaders(creationLog))
                .body(command.getProcessedBody(creationLog)).log().everything(true)
                .expect().statusCode(command.getExpectedStatus())
                .when().post(command.getProcessedURI(creationLog));

        runChecks(command.getChecks(creationLog), postResponse.prettyPrint(), context);

        if (command.getAutomaticCheck()) {
            String location = postResponse.getHeader("Location");
            log.info("Checking resource: " + location + "...");
            given().header("Accept-Encoding", "UTF-8").headers(command.getProcessedHeaders(creationLog))
                    .expect().statusCode(HttpStatus.SC_OK)
                    .when().get(location);

            if (command.getName() != null) {
                creationLog.addLocation(command.getName(), location);
            }
        }
    }

    void get(Command command, CreationLog creationLog, ApplicationContext context) throws ParseException, IOException {
        log.info("GET " + command.getProcessedURI(creationLog) + " (expecting " + command.getExpectedStatus() + ")");

        CloseableHttpClient httpClient = HttpClients.createDefault();

        String requestURI = command.getProcessedURI(creationLog);
        if (!command.getProcessedURI(creationLog).contains("http://") && !command.getProcessedURI(creationLog).contains("https://")) {
            requestURI = RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath + command.getProcessedURI(creationLog);
        }

        HttpGet httpget = new HttpGet(requestURI);
        httpget.addHeader("Content-Type", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpget);
        ResponseHandler<String> handler = new BasicResponseHandler();

        assertEquals(command.getExpectedStatus() + " expected but " + response.getStatusLine().getStatusCode() + " received.",
                response.getStatusLine().getStatusCode(), (int) command.getExpectedStatus());

        try {
            String responseBody = handler.handleResponse(response);
            runChecks(command.getChecks(creationLog), responseBody, context);
        } catch (HttpResponseException e) {
            // Nothing to do here
        }
    }

    void put(Command command, CreationLog creationLog, ApplicationContext context) throws ParseException {
        log.info("PUT " + command.getProcessedURI(creationLog) + " (expecting " + command.getExpectedStatus() + ")");
        Response r = given().contentType(JSON_UTF8).body(command.getProcessedBody(creationLog))
                .expect().statusCode(command.getExpectedStatus())
                .when().put(command.getProcessedURI(creationLog));

        runChecks(command.getChecks(creationLog), r.prettyPrint(), context);
    }

    void delete(Command command, CreationLog creationLog, ApplicationContext context) throws ParseException {
        log.info("DELETE " + command.getProcessedURI(creationLog) + " (expecting " + command.getExpectedStatus() + ")");
        Response r = given().contentType(JSON_UTF8)
                .expect().statusCode(command.getExpectedStatus())
                .when().delete(command.getProcessedURI(creationLog));

        runChecks(command.getChecks(creationLog), r.prettyPrint(), context);

        log.info("Checking resource: " + command.getProcessedURI(creationLog) + "...");
        given().contentType(JSON_UTF8)
                .expect().statusCode(HttpStatus.SC_NOT_FOUND)
                .when().get(command.getProcessedURI(creationLog));
    }

    void runChecks(Collection<Check> checks, String responseBody, ApplicationContext context) throws ParseException {
        for (Check check : checks) {
            checkRunner.verify(check, responseBody, context);
        }
    }
}
