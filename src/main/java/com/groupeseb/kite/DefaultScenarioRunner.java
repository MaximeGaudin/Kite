package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@NoArgsConstructor
public class DefaultScenarioRunner implements IScenarioRunner {
    private final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/default-beans.xml");
    private final ICommandRunner commandRunner = new DefaultCommandRunner();
    private Collection<Function> availableFunctions;

    public void execute(Scenario scenario) throws Exception {
        execute(scenario, new CreationLog());
    }

    CreationLog execute(Scenario scenario, CreationLog creationLog) throws Exception {
        availableFunctions = context.getBeansOfType(Function.class).values();

        for (Scenario dependency : scenario.getDependencies()) {
            creationLog.extend(execute(dependency, creationLog));
        }

        log.info("Testing : " + scenario.getDescription() + "...");

        for (Map.Entry<String, String> entry : scenario.getVariables().entrySet()) {
            creationLog.addVariable(entry.getKey(), applyFunctions(entry.getValue(), creationLog));
        }

        for (Command command : scenario.getCommands()) {
            commandRunner.execute(command, creationLog, context);
        }

        return creationLog;
    }

    public Function getFunction(String name) {
        for (Function availableFunction : availableFunctions) {
            if (availableFunction.match(name)) {
                return availableFunction;
            }
        }

        return null;
    }

    public String executeFunctions(String name, String body, CreationLog creationLog) {
        if (body.indexOf("{{" + name + "}}") != -1) {
            body = body.replace("{{" + name + "}}", getFunction(name).apply(new ArrayList<String>()));
        } else {
            Pattern pattern = Pattern.compile("\\{\\{" + name + "\\:(.+?)\\}\\}");
            Matcher matcher = pattern.matcher(body);

            while (matcher.find()) {
                List<String> parameters = new ArrayList<>();

                for (int i = 1; i <= matcher.groupCount(); ++i) {
                    parameters.add(creationLog.getVariableValue(matcher.group(i)));
                }

                body = body.replace(matcher.group(0), getFunction(name).apply(parameters));
            }
        }
        return body;
    }

    private String applyFunctions(String body, CreationLog creationLog) {
        String processedBody = new String(body);

        for (Function availableFunction : availableFunctions) {
            processedBody = executeFunctions(availableFunction.getName(), processedBody, creationLog);
        }

        processedBody = processedBody.replace("{{Timestamp:Now}}", DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date()));

        return processedBody;
    }
}
