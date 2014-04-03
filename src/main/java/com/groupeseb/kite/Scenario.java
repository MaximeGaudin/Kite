package com.groupeseb.kite;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Scenario {
    public static final String DESCRIPTION_KEY = "description";
    public static final String COMMANDS_KEY = "commands";
    public static final String DEPENDENCIES_KEY = "dependencies";

    @Getter
    protected String description;

    @Getter
    protected Collection<Command> commands = new ArrayList<>();

    @Getter
    protected List<Scenario> dependencies = new ArrayList<>();

    /**
     * @param filename The (class)path to the scenario file.
     * @throws IOException
     * @throws ParseException
     */
    public Scenario(String filename) throws IOException, ParseException {
        parseScenario(readFixture(filename));
    }

    protected static String readFixture(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename);

        if (!resource.exists()) {
            throw new FileNotFoundException(filename);
        }

        InputStream inputStream = resource.getInputStream();
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);

        return writer.toString();
    }

    private void parseScenario(String scenario) throws IOException, ParseException {
        Json jsonScenario = new Json(scenario);
        jsonScenario.checkExistence(new String[]{DESCRIPTION_KEY, COMMANDS_KEY});

        this.description = jsonScenario.getString(DESCRIPTION_KEY);

        for (String dependency : jsonScenario.<String>getIterable(DEPENDENCIES_KEY)) {
            dependencies.add(new Scenario(dependency));
        }

        Integer commandCount = jsonScenario.getLength(COMMANDS_KEY);
        for (Integer i = 0; i < commandCount; ++i) {
            commands.add(new Command(jsonScenario.get(COMMANDS_KEY).get(i)));
        }
    }
}
