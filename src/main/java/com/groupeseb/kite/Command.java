package com.groupeseb.kite;

import com.groupeseb.kite.check.Check;
import lombok.Getter;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

class Command {
    private static final String VERB_KEY = "verb";
    private static final String URI_KEY = "uri";

    @Getter
    private final String name;

    @Getter
    private final String description;

    @Getter
    private final Boolean disabled;

    @Getter
    private final String verb;

    @Getter
    private final String uri;

    @Getter
    private final Json body;

    @Getter
    private final Integer expectedStatus;

    @Getter
    private final Integer wait;

    @Getter
    private final Boolean automaticCheck;

    @Getter
    private final Boolean debug;

    @Getter
    private final List<Check> checks;

    public Command(Json commandSpecification) {
        commandSpecification.checkExistence(new String[]{VERB_KEY, URI_KEY});

        name = commandSpecification.getString("name");
        description = commandSpecification.getString("description");
        verb = commandSpecification.getString(VERB_KEY);
        uri = commandSpecification.getString(URI_KEY);
        wait = commandSpecification.getIntegerOrDefault("wait", 0);
        body = commandSpecification.get("body");
        disabled = commandSpecification.getBooleanOrDefault("disabled", false);
        expectedStatus = commandSpecification.getIntegerOrDefault("expectedStatus", getExpectedStatusByVerb(verb));
        automaticCheck =  commandSpecification.getBooleanOrDefault("automaticCheck", true);
        debug = commandSpecification.getBooleanOrDefault("debug", false);

        checks = new ArrayList<>();
        for (Integer i = 0; i < commandSpecification.getLength("checks"); ++i) {
            checks.add(new Check(commandSpecification.get("checks").get(i)));
        }
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
}