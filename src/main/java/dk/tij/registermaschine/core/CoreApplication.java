package dk.tij.registermaschine.core;

import dk.tij.registermaschine.core.config.InstructionConfigParser;
import dk.tij.registermaschine.core.config.InstructionRegistry;

import java.io.File;

public class CoreApplication {
    static void main() throws Exception {
        InstructionRegistry registry = new InstructionRegistry();
        InstructionConfigParser parser = new InstructionConfigParser(registry);

        parser.parseConfig(new File("src/main/resources/configuration.xml"));

        System.out.println(registry.Map());
    }
}
