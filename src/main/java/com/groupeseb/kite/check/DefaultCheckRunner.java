package com.groupeseb.kite.check;

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.JsonPath;
import com.jayway.restassured.response.Response;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class DefaultCheckRunner implements ICheckRunner {
    protected static final Logger LOG = LoggerFactory.getLogger(DefaultCheckRunner.class);

    private ICheckOperator getMatchingOperator(String operatorName, ApplicationContext factory) {
        ICheckOperator match = null;
        Integer matchCount = 0;

        for (ICheckOperator operator : factory.getBeansOfType(ICheckOperator.class).values()) {
            if (operator.match(operatorName)) {
                match = operator;
                matchCount++;
            }

            if (matchCount > 1) {
                throw new UnsupportedOperationException("Several (" + matchCount.toString() + ") operators match but only one is allowed.");
            }
        }

        if (matchCount == 0) {
            throw new IndexOutOfBoundsException("No matching operator found for '" + operatorName + "'");
        }

        return match;
    }

    private ICheckMethod getMatchingMethod(String methodName, ApplicationContext factory) {
        ICheckMethod match = null;
        Integer matchCount = 0;

        for (ICheckMethod operator : factory.getBeansOfType(ICheckMethod.class).values()) {
            if (operator.match(methodName)) {
                match = operator;
                matchCount++;
            }

            if (matchCount > 1) {
                throw new UnsupportedOperationException("Several (" + matchCount.toString() + ") operators match but only one match is allowed.");
            }
        }

        if (matchCount == 0) {
            throw new IndexOutOfBoundsException("No matching method found for '" + methodName + "'");
        }

        return match;
    }

    @Override
    public void verify(Check check, Response r, ApplicationContext context) throws ParseException {
        LOG.info("Checking " + check.getDescription() + "...");

        if(check.getSkip()) {
            LOG.warn("Check skipped (" + check.getDescription() + ")");
            return ;
        }

        ICheckOperator operator = getMatchingOperator(check.getOperatorName(), context);
        ICheckMethod method = getMatchingMethod(check.getMethodName(), context);

        Object node = JsonPath.read(r.prettyPrint(), check.getFieldName());
        if (check.getForeach()) {
            Preconditions.checkArgument(node instanceof Iterable, "Using 'forEach' mode for check requires an iterable node.");

            @SuppressWarnings({"unchecked", "ConstantConditions"})
            Iterable nodeList = (Iterable)node;

            if (check.getMustMatch()) {
                Preconditions.checkArgument(nodeList.iterator().hasNext(), "No match found but 'mustMatch' was set to true.");
            }

            for (Object o : nodeList) {
                operator.apply(method.apply(o, check.getParameters()), check.getExpectedValue(), check.getDescription());
            }
        } else {
            operator.apply(method.apply(node, check.getParameters()), check.getExpectedValue(), check.getDescription());
        }
    }
}
