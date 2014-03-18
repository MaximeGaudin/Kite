package com.groupeseb.kite.check.impl.methods;


import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

@Component
public class ExistsMethod implements ICheckMethod {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("exists");
    }

    @Override
    public Object apply(Object obj) {
        return obj == null ? "false" : "true";
    }
}
