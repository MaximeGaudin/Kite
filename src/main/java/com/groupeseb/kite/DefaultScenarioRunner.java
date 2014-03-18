package com.groupeseb.kite;

import lombok.NoArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;


@NoArgsConstructor
public class DefaultScenarioRunner implements IScenarioRunner {
    protected static final Logger LOG = LoggerFactory.getLogger(Command.class);

    protected ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/default-beans.xml");

    ICommandRunner commandRunner = new DefaultCommandRunner();

    public void execute(Scenario scenario, Boolean postCheckEnabled) throws Exception {
        execute(scenario, postCheckEnabled, new CreationLog());
    }

    protected CreationLog execute(Scenario scenario, Boolean postCheckEnabled, CreationLog creationLog) throws Exception {
        for (Scenario dependency : scenario.getDependencies()) {
            creationLog.extend(execute(dependency, postCheckEnabled, creationLog));
        }

        LOG.info("Testing : " + scenario.getDescription() + "...");

        for (Command command : scenario.getCommands()) {
            commandRunner.execute(command, postCheckEnabled, creationLog, context);
        }

        return creationLog;
    }
}
