package com.groupeseb.kite.check.impl.operators;


import com.google.common.base.Preconditions;
import com.groupeseb.kite.check.ICheckOperator;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static org.testng.Assert.assertTrue;

@Component
public class GreaterThanOperator implements ICheckOperator {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("gt");
    }

    @Override
    public void apply(Object value, Object expected, String description) {
        Preconditions.checkArgument(
                Number.class.isAssignableFrom(value.getClass()),
                "The input argument of 'gt' must be a number"
        );

        Preconditions.checkArgument(
                Number.class.isAssignableFrom(expected.getClass()),
                "The input argument of 'gt' must be a number"
        );

        assertTrue(
                ((Number) value).doubleValue() > ((Number) expected).doubleValue()
                , description
        );
    }
}
