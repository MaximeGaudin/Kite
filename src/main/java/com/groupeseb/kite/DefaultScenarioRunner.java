package com.groupeseb.kite;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


@Slf4j
@NoArgsConstructor
public class DefaultScenarioRunner implements IScenarioRunner {
    private final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/default-beans.xml");
    private final ICommandRunner commandRunner = new DefaultCommandRunner();

    public void execute(Scenario scenario) throws Exception {
        execute(scenario, new CreationLog());
    }

    CreationLog execute(Scenario scenario, CreationLog creationLog) throws Exception {
        for (Scenario dependency : scenario.getDependencies()) {
            creationLog.extend(execute(dependency, creationLog));
        }

        log.info("Testing : " + scenario.getDescription() + "...");

        for (Command command : scenario.getCommands()) {
            commandRunner.execute(command, creationLog, context);
        }

        return creationLog;
    }
}
