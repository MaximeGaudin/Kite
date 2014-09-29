package com.groupeseb.kite.check;

import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;

public interface ICheckRunner {
    void verify(Check check, String responseBody, ApplicationContext context) throws ParseException;
}
