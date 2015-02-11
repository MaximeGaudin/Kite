package com.groupeseb.kite.check.impl.operators;

import com.groupeseb.kite.check.ICheckOperator;
import org.springframework.stereotype.Component;

import static org.testng.Assert.assertEquals;

@Component
public class EqualsOperator implements ICheckOperator {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("equals");
    }

    @Override
    public void apply(Object value, Object expected, String description) {
        if (value == null) {
            assertEquals(value, expected, description);
        }

        if (Number.class.isAssignableFrom(value.getClass()) &&
                Number.class.isAssignableFrom(expected.getClass())) {
            assertEquals(((Number) value).doubleValue(), ((Number) expected).doubleValue(), description);
        } else if (Number.class.isAssignableFrom(value.getClass())) {
            try {
                assertEquals(((Number) value).doubleValue(), Double.valueOf(expected.toString()), description);
            } catch (NumberFormatException e) {
                assertEquals(value, expected, description);
            }
        } else {
            assertEquals(value, expected, description);
        }
    }
}
