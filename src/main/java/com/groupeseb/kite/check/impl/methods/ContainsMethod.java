package com.groupeseb.kite.check.impl.methods;


import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

import static org.testng.Assert.assertTrue;

@Component
public class ContainsMethod implements ICheckMethod {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("contains");
    }

    @Override
    public Object apply(Object obj, Json parameters) {
        // Only string are supported
        assertTrue (obj instanceof String, "Only strings are supported for the contains method");

        for(Integer i = 0; i < parameters.getLength(); ++i) {
           if(!((String) obj).contains(parameters.getString(i))) {
               return false;
           }
        }

        return true;
    }
}
