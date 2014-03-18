package com.groupeseb.kite;

import com.groupeseb.kite.check.Check;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

class Command {
    protected static final Logger LOG = LoggerFactory.getLogger(Command.class);
    public static final String VERB_KEY = "verb";
    public static final String URI_KEY = "uri";

    @Getter
    protected String name;

    @Getter
    protected String description;

    @Getter
    protected String verb;

    @Getter
    protected String uri;

    @Getter
    protected Json body;

    @Getter
    protected Integer expectedStatus;

    @Getter
    protected Integer wait;

    @Getter
    protected List<Check> checks;

    public Command(Json commandSpecification) {
        commandSpecification.checkExistence(new String[]{VERB_KEY, URI_KEY});

        name = commandSpecification.getString("name");
        description = commandSpecification.getString("description");
        verb = commandSpecification.getString(VERB_KEY);
        uri = commandSpecification.getString(URI_KEY);
        wait = commandSpecification.getInteger("wait", 0);
        body = commandSpecification.get("body");
        expectedStatus = commandSpecification.getInteger("expectedStatus", getExpectedStatusByVerb(verb));

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