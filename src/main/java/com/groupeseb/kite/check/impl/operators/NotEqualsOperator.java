package com.groupeseb.kite.check.impl.operators;

import com.groupeseb.kite.check.ICheckOperator;
import org.springframework.stereotype.Component;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

@Component
public class NotEqualsOperator implements ICheckOperator {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("notequals");
    }

    @Override
    public void apply(Object value, Object expected, String description) {
        assertTrue(((Comparable) value).compareTo((Comparable) expected) != 1, description);
    }
}
