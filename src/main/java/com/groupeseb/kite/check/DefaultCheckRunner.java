package com.groupeseb.kite.check;

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;

@Slf4j
public class DefaultCheckRunner implements ICheckRunner {
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
    public void verify(Check check, String responseBody, ApplicationContext context) throws ParseException {
        log.info("Checking " + check.getDescription() + "...");

        if (check.getSkip()) {
            log.warn("Check skipped (" + check.getDescription() + ")");
            return;
        }

        ICheckOperator operator = getMatchingOperator(check.getOperatorName(), context);
        ICheckMethod method = getMatchingMethod(check.getMethodName(), context);

        Object node = JsonPath.read(responseBody, check.getFieldName());
        if (check.getForeach()) {
            Preconditions.checkArgument(node instanceof Iterable, "Using 'forEach' mode for check requires an iterable node.");

            @SuppressWarnings({"unchecked", "ConstantConditions"})
            Iterable nodeList = (Iterable) node;

            if (check.getMustMatch()) {
                Preconditions.checkArgument(nodeList.iterator().hasNext(), check.getDescription() + " (No match found but 'mustMatch' was set to true)");
            }

            for (Object o : nodeList) {
                operator.apply(method.apply(o, check.getParameters()), parseExpectedValue(check.getExpectedValue(), responseBody), check.getDescription());
            }
        } else {
            operator.apply(method.apply(node, check.getParameters()), parseExpectedValue(check.getExpectedValue(), responseBody), check.getDescription());
        }
    }

    private Object parseExpectedValue(Object expectedValue, String responseBody) {
//        if (String.class.isAssignableFrom(expectedValue.getClass())) {
//            Pattern lookupPattern = Pattern.compile("\\{\\{Lookup\\:%\\.(.+)\\}\\}");
//            Matcher matcher = lookupPattern.matcher(expectedValue.toString());
//
//            if (matcher.find()) {
//                String path = matcher.group(1);
//                return JsonPath.read(responseBody, path);
//            }
//        }

        return expectedValue;
    }
}
