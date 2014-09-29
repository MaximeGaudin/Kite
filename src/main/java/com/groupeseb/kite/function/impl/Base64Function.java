package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import java.util.List;

@Slf4j
@Component
public class Base64Function extends Function {
    @Override
    public String getName() {
        return "Base64";
    }

    @Override
    public String apply(List<String> parameters) {
        Preconditions.checkArgument(parameters.size() == 1, "Exactly one parameter is needed");

        return new BASE64Encoder().encode(parameters.get(0).getBytes());
    }
}
