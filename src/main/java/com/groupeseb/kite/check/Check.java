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
    protected final String fieldName;

    @Getter
    protected final String methodName;

    @Getter
    protected final String operatorName;

    @Getter
    protected final String expectedValue;

    @Getter
    protected final Json parameters;

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
    }
}
