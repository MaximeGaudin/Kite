package com.groupeseb.kite.check;

import com.groupeseb.kite.Json;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Check {
    protected static final Logger LOG = LoggerFactory.getLogger(Check.class);

    @Getter
    private final String description;

    @Getter
    private final String fieldName;

    @Getter
    private final String methodName;

    @Getter
    private final String operatorName;

    @Getter
    private final String expectedValue;

    @Getter
    private final Json parameters;

    @Getter
    private final Boolean foreach;

    @Getter
    private final Boolean mustMatch;

    @Getter
    private final Boolean skip;

    public Check(Json checkSpecification) {
        checkSpecification.checkExistence(new String[]{"field", "expected"});

        if (!checkSpecification.exists("description")) {
            LOG.warn("'description' field is missing in one of your check.");
        }

        description = (checkSpecification.getString("description") == null) ? "" : checkSpecification.getString("description");
        fieldName = checkSpecification.getString("field");
        methodName = (checkSpecification.getString("method") == null) ? "nop" : checkSpecification.getString("method");
        operatorName = (checkSpecification.getString("operator") == null) ? "equals" : checkSpecification.getString("operator");
        expectedValue = checkSpecification.getString("expected");
        parameters = checkSpecification.get("parameters");
        foreach = checkSpecification.getBooleanOrDefault("foreach", fieldName.contains("*"));
        mustMatch = checkSpecification.getBooleanOrDefault("mustMatch", foreach);
        skip = checkSpecification.getBooleanOrDefault("skip", false);
    }
}
