package com.groupeseb.kite.check.impl.operators;


import com.groupeseb.kite.check.ICheckOperator;
import org.springframework.stereotype.Component;

import static org.testng.Assert.assertTrue;

@Component
public class GreaterThanOperator implements ICheckOperator {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("gt");
    }

    @Override
    public void apply(Object value, Object expected, String description) {
        assertTrue(Double.valueOf(value.toString()) >= Double.valueOf(expected.toString()), description);
    }
}
