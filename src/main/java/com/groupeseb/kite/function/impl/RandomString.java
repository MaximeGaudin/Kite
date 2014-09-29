package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class RandomString extends Function {
    public String getName() {
        return "RandomString";
    }

    public String apply(List<String> parameters) {
        return UUID.randomUUID().toString();
    }
}
