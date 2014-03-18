package com.groupeseb.kite.check;

import com.jayway.restassured.response.Response;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;

public interface ICheckRunner {
    void verify(Check check, Response r, ApplicationContext context) throws ParseException;
}
