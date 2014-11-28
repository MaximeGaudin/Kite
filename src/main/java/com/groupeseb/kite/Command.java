package com.groupeseb.kite;

import com.groupeseb.kite.check.Check;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
class Command {
    private static final String VERB_KEY = "verb";
    private static final String URI_KEY = "uri";

    private final String name;
    private final String description;
    private final Boolean disabled;
    private final String verb;
    private final String uri;
    private final Json body;
    private final Integer expectedStatus;
    private final Integer wait;
    private final Boolean automaticCheck;
    private final Boolean debug;
    private final Map<String, String> headers;

    private Pagination pagination;

    private final Json commandSpecification;

    public Command(Json commandSpecification) {
        this.commandSpecification = commandSpecification;

        commandSpecification.checkExistence(new String[]{VERB_KEY, URI_KEY});

        name = commandSpecification.getString("name");
        description = commandSpecification.getString("description");
        verb = commandSpecification.getString(VERB_KEY);
        uri = commandSpecification.getString(URI_KEY);
        wait = commandSpecification.getIntegerOrDefault("wait", 0);
        body = commandSpecification.get("body");
        disabled = commandSpecification.getBooleanOrDefault("disabled", false);
        expectedStatus = commandSpecification.getIntegerOrDefault("expectedStatus", getExpectedStatusByVerb(verb));
        automaticCheck = commandSpecification.getBooleanOrDefault("automaticCheck", expectedStatus.toString().startsWith("2") ? true : false);
        debug = commandSpecification.getBooleanOrDefault("debug", false);

        if (commandSpecification.exists("pagination")) {
            pagination = new Pagination(commandSpecification.get("pagination"));
        }

        headers = commandSpecification.getMap("headers");
    }

    public List<Check> getChecks(CreationLog creationLog) throws ParseException {
        List<Check> checks = new ArrayList<>();
        for (Integer i = 0; i < commandSpecification.getLength("checks"); ++i) {
            checks.add(new Check(commandSpecification.get("checks").get(i), creationLog));
        }

        return checks;
    }

    private Integer getExpectedStatusByVerb(String string) {
        switch (string) {
            case ("POST"):
                return HttpStatus.SC_CREATED;
            case ("PUT"):
                return HttpStatus.SC_NO_CONTENT;
            case ("GET"):
                return HttpStatus.SC_OK;
            case ("DELETE"):
                return HttpStatus.SC_NO_CONTENT;
            case ("HEAD"):
                return HttpStatus.SC_OK;

            default:
                return HttpStatus.SC_OK;
        }
    }

    String getProcessedURI(CreationLog creationLog) {
        return creationLog.processPlaceholders(getName(), getUri());
    }

    String getProcessedBody(CreationLog creationLog) {
        if (getBody() == null) {
            return "";
        }
        return creationLog.processPlaceholders(getName(), getBody().toString());
    }

    Map<String, String> getProcessedHeaders(CreationLog creationLog) {
        Map<String, String> processedHeaders = new HashMap<>(getHeaders());

        for (Map.Entry<String, String> entry : processedHeaders.entrySet()) {
            processedHeaders.put(entry.getKey(), creationLog.processPlaceholders(getName(), processedHeaders.get(entry.getKey())));
        }

        return processedHeaders;
    }
}