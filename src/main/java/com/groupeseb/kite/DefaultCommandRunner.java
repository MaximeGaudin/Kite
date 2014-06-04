package com.groupeseb.kite;

import com.groupeseb.kite.check.Check;
import com.groupeseb.kite.check.DefaultCheckRunner;
import com.groupeseb.kite.check.ICheckRunner;
import com.jayway.restassured.response.Response;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.given;

@NoArgsConstructor
@Component
public class DefaultCommandRunner implements ICommandRunner {
    protected static final Logger LOG = LoggerFactory.getLogger(Command.class);

    public static final String JSON_UTF8 = ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), "UTF-8").toString();
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";

    ICheckRunner checkRunner = new DefaultCheckRunner();

    @Override
    public void execute(Command command, CreationLog creationLog, ApplicationContext context) throws Exception {
        if (command.getDescription() != null) {
            LOG.info(command.getDescription() + "...");
        }

        if (command.getDisabled()) {
            LOG.warn("Disabled command : Skipped.");
            return;
        }

        if (command.getWait() > 0) {
            LOG.info("Waiting for " + command.getWait() + "ms...");
            Thread.sleep(command.getWait());
        }

        String processedUri = processPlaceholders(command.getName(), command.getUri(), creationLog);
        String processedBody = (command.getBody() == null) ? null : processPlaceholders(command.getName(), command.getBody().toString(), creationLog);

        switch (command.getVerb().toUpperCase()) {
            case (POST):
                post(command.getName(), processedUri, command.getExpectedStatus(), processedBody, command.getChecks(),
                        command.getAutomaticCheck(), command.getDebug(),
                        creationLog,
                        context);
                break;
            case (GET):
                get(processedUri, command.getExpectedStatus(), command.getChecks(), context);
                break;
            case (PUT):
                put(processedUri, command.getExpectedStatus(), processedBody, command.getChecks(), context);
                break;
            case (DELETE):
                delete(processedUri, command.getExpectedStatus(), command.getChecks(), context);
                break;
        }
    }

    protected void post(String name, String uri, Integer expectedStatus, String body, Collection<Check> checks, Boolean postCheckEnabled, Boolean debug, CreationLog creationLog, ApplicationContext context) throws ParseException {
        LOG.info("[" + name + "] POST " + uri + " (expecting " + expectedStatus + ")");

        if (debug) {
            LOG.info("[" + name + "] " + body);
        }

        Response postResponse = given().contentType(JSON_UTF8).body(body)
                .expect().statusCode(expectedStatus)
                .when().post(uri);

        runChecks(checks, postResponse, context);

        if (postCheckEnabled) {
            String location = postResponse.getHeader("Location");
            LOG.info("Checking resource: " + location + "...");
            given().header("Accept-Encoding", "UTF-8")
                    .expect().statusCode(HttpStatus.SC_OK)
                    .when().get(location);

            if (name != null) {
                creationLog.addLocation(name, location);
            }
        }
    }

    protected void get(String uri, Integer expectedStatus, Collection<Check> checks, ApplicationContext context) throws ParseException {
        LOG.info("GET " + uri + " (expecting " + expectedStatus + ")");
        Response r = given().contentType(JSON_UTF8)
                .expect().statusCode(expectedStatus)
                .when().get(uri);

        runChecks(checks, r, context);
    }

    protected void put(String uri, Integer expectedStatus, String body, Collection<Check> checks, ApplicationContext context) throws ParseException {
        LOG.info("PUT " + uri + " (expecting " + expectedStatus + ")");
        Response r = given().contentType(JSON_UTF8).body(body)
                .expect().statusCode(expectedStatus)
                .when().put(uri);

        runChecks(checks, r, context);
    }

    protected void delete(String uri, Integer expectedStatus, Collection<Check> checks, ApplicationContext context) throws ParseException {
        LOG.info("DELETE " + uri + " (expecting " + expectedStatus + ")");
        Response r = given().contentType(JSON_UTF8)
                .expect().statusCode(expectedStatus)
                .when().delete(uri);

        runChecks(checks, r, context);

        LOG.info("Checking resource: " + uri + "...");
        given().contentType(JSON_UTF8)
                .expect().statusCode(HttpStatus.SC_NOT_FOUND)
                .when().get(uri);
    }

    protected void runChecks(Collection<Check> checks, Response r, ApplicationContext context) throws ParseException {
        for (Check check : checks) {
            checkRunner.verify(check, r, context);
        }
    }

    private Map<String, String> getUUIDs(String scenario, CreationLog creationLog) {
        Pattern uuidPattern = Pattern.compile("\\{\\{UUID:(.+?)\\}\\}");
        Matcher uuidMatcher = uuidPattern.matcher(scenario);

        Map<String, String> uuids = new HashMap<>();

        while (uuidMatcher.find()) {
            String name = uuidMatcher.group(1);

            if (!creationLog.getUUIDs().containsKey(name)) {
                uuids.put(name, UUID.randomUUID().toString());
            }
        }

        return uuids;
    }

    private String processPlaceholders(String name, String body, CreationLog creationLog) {
        String processedBody = body.replace("{{UUID}}", "{{UUID:" + name + "}}");

        Map<String, String> uuids = getUUIDs(processedBody, creationLog);
        creationLog.addUUIDs(uuids);

        for (Map.Entry<String, String> entry : creationLog.getUUIDs().entrySet()) {
            processedBody = processedBody.replace("{{UUID:" + entry.getKey() + "}}", entry.getValue());
        }

        for (Map.Entry<String, String> entry : creationLog.getLocations().entrySet()) {
            processedBody = processedBody.replace("{{Location:" + entry.getKey() + "}}", entry.getValue());
        }

        processedBody = processedBody.replace("{{Timestamp:Now}}", DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date()));

        return processedBody;
    }

}
