package com.groupeseb.kite.check.impl.operators;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.check.ICheckOperator;
import org.springframework.stereotype.Component;

import static org.testng.Assert.assertTrue;

@Component
public class NotEqualsOperator implements ICheckOperator {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("notequals");
    }


    @SuppressWarnings("unchecked")
    @Override
    public void apply(Object value, Object expected, String description) {
        Preconditions.checkArgument(value instanceof Comparable, "Using 'equals' or 'notEquals' operators requires Comparable objects.");
        assertTrue(((Comparable) value).compareTo(expected) != 0, description);
    }
}
