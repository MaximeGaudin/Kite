package com.groupeseb.kite.check.impl.methods;


import com.google.common.base.Preconditions;
import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

@Component
public class ContainsMethod implements ICheckMethod {
    @Override
    public Boolean match(String name) {
        return name.equalsIgnoreCase("contains");
    }

    @Override
    public Object apply(Object obj, Json parameters) {
        Preconditions.checkArgument(obj instanceof String,
                "Only strings are supported by the " + this.getClass().getName() + " method");

        for (Integer i = 0; i < parameters.getLength(); ++i) {
            if (!((String) obj).contains(parameters.getString(i))) {
                return false;
            }
        }

        return true;
    }
}
