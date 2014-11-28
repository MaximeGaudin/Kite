package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.simple.parser.ParseException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@Data
public class CreationLog {
    private final Map<String, String> uuids = new HashMap<>();
    private final Map<String, String> locations = new HashMap<>();
    private Map<String, String> variables = new HashMap<>();
    private Collection<Function> availableFunctions;

    public CreationLog(Collection<Function> availableFunctions) {
        this.availableFunctions = availableFunctions;
    }

    public void extend(CreationLog creationLog) {
        this.uuids.putAll(creationLog.uuids);
        this.locations.putAll(creationLog.locations);
        this.variables.putAll(creationLog.variables);
    }

    public void addLocation(String name, String location) {
        locations.put(name, location);
    }

    public void addUUIDs(Map<String, String> uuids) {
        this.uuids.putAll(uuids);
    }

    public void addVariable(String key, String value) {
        this.variables.put(key, value);
    }

    public String getVariableValue(String variableName) {
        return this.variables.get(variableName.trim());
    }

    private Map<String, String> getEveryUUIDs(String scenario) {
        Pattern uuidPattern = Pattern.compile("\\{\\{UUID:(.+?)\\}\\}");
        Matcher uuidMatcher = uuidPattern.matcher(scenario);

        Map<String, String> uuids = new HashMap<>();

        while (uuidMatcher.find()) {
            String name = uuidMatcher.group(1);

            if (!this.getUuids().containsKey(name)) {
                uuids.put(name, UUID.randomUUID().toString());
            }
        }

        return uuids;
    }

    public Function getFunction(String name) {
        for (Function availableFunction : availableFunctions) {
            if (availableFunction.match(name)) {
                return availableFunction;
            }
        }

        return null;
    }

    String executeFunctions(String name, String body) {
        Pattern withoutParameters = Pattern.compile("\\{\\{" + name + "\\}\\}", Pattern.CASE_INSENSITIVE);

        if (withoutParameters.matcher(body).find()) {
            body = withoutParameters.matcher(body).replaceAll(getFunction(name).apply(new ArrayList<String>()));
        } else {
            Pattern pattern = Pattern.compile("\\{\\{" + name + "\\:(.+?)\\}\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(body);

            while (matcher.find()) {
                List<String> parameters = new ArrayList<>();

                for (int i = 1; i <= matcher.groupCount(); ++i) {
                    parameters.add(this.getVariableValue(matcher.group(i)));
                }

                body = body.replace(matcher.group(0), getFunction(name).apply(parameters));
            }
        }
        return body;
    }

    String applyFunctions(String body) {
        String processedBody = new String(body);

        for (Function availableFunction : availableFunctions) {
            processedBody = executeFunctions(availableFunction.getName(), processedBody);
        }

        processedBody = processedBody.replace("{{Timestamp:Now}}", DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date()));

        return processedBody;
    }

    public String processPlaceholdersInString(String body) {
        return processPlaceholders(null, body);
    }

    public String processPlaceholders(String commandName, String body) {
        String processedBody = new String(body);

        if (commandName != null) {
            processedBody = processedBody.replace("{{UUID}}", "{{UUID:" + commandName + "}}");
        }

        this.uuids.putAll(getEveryUUIDs(processedBody));

        for (Map.Entry<String, String> entry : getVariables().entrySet()) {
            processedBody = processedBody.replace("{{Variable:" + entry.getKey() + "}}", entry.getValue());
        }

        for (Map.Entry<String, String> entry : this.getUuids().entrySet()) {
            processedBody = processedBody.replace("{{UUID:" + entry.getKey() + "}}", entry.getValue());
        }

        for (Map.Entry<String, String> entry : getLocations().entrySet()) {
            processedBody = processedBody.replace("{{Location:" + entry.getKey() + "}}", entry.getValue());
        }

        processedBody = applyFunctions(processedBody);

        return processedBody;
    }

    public Object processPlaceholders(Object expected) throws ParseException {
        if (expected instanceof String) {
            return processPlaceholdersInString(expected.toString());
        } else if (expected instanceof Json) {
            Json expectedObject = (Json) expected;
            return new Json(processPlaceholdersInString(expectedObject.toString()));
        } else if (expected instanceof Boolean) {
            return expected;
        } else if (expected instanceof Long) {
                return expected;
        }  else if (expected instanceof Double) {
            return expected;
        } else {
            throw new NotImplementedException();
        }
    }
}
