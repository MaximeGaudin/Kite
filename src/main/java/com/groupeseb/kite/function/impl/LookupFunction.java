package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.testng.Assert.fail;

@Slf4j
@Component
public class LookupFunction extends Function {
    public String getName() {
        return "Lookup";
    }

    public String apply(List<String> parameters, CreationLog creationLog) {
        String objectName = parameters.get(0).split("\\.")[0];
        String field = parameters.get(0).replace(objectName + ".", "");

        if (creationLog.getBody(objectName) == null) {
            fail(String.format("No payload found for %s. Are you sure any request name %s was performed ?", objectName, objectName));
        }

        return JsonPath.read(creationLog.getBody(objectName), field).toString();
    }
}
