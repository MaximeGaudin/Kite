package com.groupeseb.kite.check;

public interface ICheckOperator {
    Boolean match(String name);

    void apply(Object value, Object expected, String description);
}
