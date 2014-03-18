package com.groupeseb.kite;

import org.springframework.context.ApplicationContext;

public interface ICommandRunner {
    void execute(Command command, Boolean postCheckEnabled, CreationLog creationLog, ApplicationContext context) throws Exception;
}