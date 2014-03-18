package com.groupeseb.kite.check.impl.methods;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

import static com.jayway.restassured.path.json.JsonPath.from;

@Component
public class NopMethod implements ICheckMethod {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("nop");
    }

    @Override
    public Object apply(Object obj) {
        return obj;
    }
}
