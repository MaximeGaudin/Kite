package com.groupeseb.kite.check;

import com.groupeseb.kite.Json;

public interface ICheckMethod {
    Boolean match(String name);

    Object apply(Object obj, Json parameters);
}
