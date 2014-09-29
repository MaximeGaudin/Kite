package com.groupeseb.kite.function;

import java.util.List;

public abstract class Function {
    public Boolean match(String name) {
        return name.trim().toUpperCase().equals(this.getName().trim().toUpperCase());
    }

    public abstract String getName();

    public abstract String apply(List<String> parameters);
}
