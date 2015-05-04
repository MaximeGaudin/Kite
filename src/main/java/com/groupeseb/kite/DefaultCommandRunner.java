package com.groupeseb.kite;

import com.groupeseb.kite.check.Check;
import com.groupeseb.kite.check.DefaultCheckRunner;
import com.groupeseb.kite.check.ICheckRunner;
import com.groupeseb.kite.function.Function;
import com.jayway.jsonpath.JsonPath;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

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
                .body(command.getProcessedBody(creationLog))
                .when().post(command.getProcessedURI(creationLog));

        String response = postResponse.prettyPrint();
        log.info(response);

        creationLog.addBody("%", response);
        if (command.getName() != null) {
            creationLog.addBody(command.getName(), response);
        }

        assertEquals("Unexpected response status", command.getExpectedStatus(), new Integer(postResponse.getStatusCode()));
        runChecks(command.getChecks(creationLog), response, context);

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

    private String performGetRequest(Command command, CreationLog creationLog, ApplicationContext context, @Nullable HttpParams params) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String requestURI = command.getProcessedURI(creationLog);
        if (!command.getProcessedURI(creationLog).contains("http://") && !command.getProcessedURI(creationLog).contains("https://")) {
            requestURI = RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath + command.getProcessedURI(creationLog);
        }

        HttpGet httpget = new HttpGet(requestURI);

        if (params != null) {
            httpget.setParams(httpget.getParams());
        }

        httpget.addHeader("Content-Type", "application/json");
        for (Map.Entry<String, String> header : command.getProcessedHeaders(creationLog).entrySet()) {
            httpget.addHeader(header.getKey(), header.getValue());
        }

        CloseableHttpResponse response = httpClient.execute(httpget);
        ResponseHandler<String> handler = new BasicResponseHandler();

        assertEquals(command.getDescription() + " | " + command.getExpectedStatus() + " expected but " + response.getStatusLine().getStatusCode() + " received.",
                (int) command.getExpectedStatus(), response.getStatusLine().getStatusCode());

        try {
            String body = handler.handleResponse(response);
            creationLog.addBody("%", body);
            if (command.getName() != null) {
                creationLog.addBody(command.getName(), body);
            }

            return body;
        } catch (Exception e) {
            return "";
        }
    }

    void get(Command command, CreationLog creationLog, ApplicationContext context) throws ParseException, IOException {
        if (command.getPagination() != null) {
            paginatedGet(command, creationLog, context);
        } else {
            String responseBody = performGetRequest(command, creationLog, context, null);
            runChecks(command.getChecks(creationLog), responseBody, context);
        }
    }

    void paginatedGet(Command command, CreationLog creationLog, ApplicationContext context) throws ParseException, IOException {
        log.info("GET " + command.getProcessedURI(creationLog) + " (expecting " + command.getExpectedStatus() + ")");

        Integer currentPage = command.getPagination().getStartPage();
        Integer totalPages = currentPage;

        while (currentPage <= totalPages) {
            BasicHttpParams params = new BasicHttpParams();
            params.setParameter(command.getPagination().getPageParameterName(), command.getPagination().getStartPage());
            params.setParameter(command.getPagination().getSizeParameterName(), command.getPagination().getSize());

            String responseBody = performGetRequest(command, creationLog, context, params);
            totalPages = JsonPath.read(responseBody, command.getPagination().getTotalPagesField());

            runChecks(command.getChecks(creationLog), responseBody, context);
            currentPage++;
        }
    }

    void put(Command command, CreationLog creationLog, ApplicationContext context) throws ParseException {
        log.info("[" + command.getName() + "] PUT " + command.getProcessedURI(creationLog) + " (expecting " + command.getExpectedStatus() + ")");

        if (command.getDebug()) {
            log.info("[" + command.getName() + "] " + command.getProcessedBody(creationLog));
        }

        Response postResponse = given()
                .contentType(JSON_UTF8).headers(command.getProcessedHeaders(creationLog))
                .body(command.getProcessedBody(creationLog)).log().everything(true)
                .expect().statusCode(command.getExpectedStatus())
                .when().put(command.getProcessedURI(creationLog));

        String response = postResponse.prettyPrint();
        log.info(response);

        creationLog.addBody("%", response);
        if (command.getName() != null) {
            creationLog.addBody(command.getName(), response);
        }

        runChecks(command.getChecks(creationLog), response, context);
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
            try {
                checkRunner.verify(check, responseBody, context);
            } catch (RuntimeException e) {
                fail("Check [" + check.getDescription() + "] failed : " + e.getMessage());
            }
        }
    }
}
