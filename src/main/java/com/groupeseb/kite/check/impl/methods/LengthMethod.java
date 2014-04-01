package com.groupeseb.kite.check.impl.methods;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class LengthMethod implements ICheckMethod {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("length");
    }

    @Override
    public Object apply(Object o, Json parameters) {
        return Integer.toString(((Collection)o).size());
    }
}
