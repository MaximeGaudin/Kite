package com.groupeseb.kite.check;

public interface ICheckMethod {
    Boolean match(String name);

    Object apply(Object obj);
}
