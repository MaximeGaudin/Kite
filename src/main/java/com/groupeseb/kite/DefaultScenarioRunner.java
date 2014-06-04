package com.groupeseb.kite;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


@NoArgsConstructor
public class DefaultScenarioRunner implements IScenarioRunner {
    protected static final Logger LOG = LoggerFactory.getLogger(Command.class);

    protected ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/default-beans.xml");

    ICommandRunner commandRunner = new DefaultCommandRunner();

    public void execute(Scenario scenario) throws Exception {
        execute(scenario, new CreationLog());
    }

    protected CreationLog execute(Scenario scenario, CreationLog creationLog) throws Exception {
        for (Scenario dependency : scenario.getDependencies()) {
            creationLog.extend(execute(dependency, creationLog));
        }

        LOG.info("Testing : " + scenario.getDescription() + "...");

        for (Command command : scenario.getCommands()) {
            commandRunner.execute(command, creationLog, context);
        }

        return creationLog;
    }
}
