package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;


@Slf4j
@NoArgsConstructor
public class DefaultScenarioRunner implements IScenarioRunner {
    private final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/default-beans.xml");
    private final ICommandRunner commandRunner = new DefaultCommandRunner();

    public void execute(Scenario scenario) throws Exception {
        execute(scenario, new CreationLog(context.getBeansOfType(Function.class).values()));
    }

    private CreationLog execute(Scenario scenario, CreationLog creationLog) throws Exception {
        log.info("Parsing {}...", scenario.getFilename());

        for (Scenario dependency : scenario.getDependencies()) {
            creationLog.extend(execute(dependency, creationLog));
        }

        log.info("Executing {}...", scenario.getFilename());
        log.info("Testing : " + scenario.getDescription() + "...");

        for (Map.Entry<String, Object> entry : scenario.getVariables().entrySet()) {
            creationLog.addVariable(entry.getKey(), creationLog.applyFunctions(entry.getValue().toString()));
        }

        for (Command command : scenario.getCommands()) {
            commandRunner.execute(command, creationLog, context);
        }

        return creationLog;
    }
}
