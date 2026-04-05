package dk.tij.rm;

import dk.tij.registermaschine.api.instructions.AbstractInstruction;
import dk.tij.registermaschine.api.plugin.IPlugin;
import dk.tij.registermaschine.api.plugin.PluginContext;
import dk.tij.rm.instructions.AdditionInstruction;
import dk.tij.rm.instructions.DivisionInstruction;
import dk.tij.rm.instructions.HaltInstruction;
import dk.tij.rm.instructions.InputInstruction;
import dk.tij.rm.instructions.JumpInstruction;
import dk.tij.rm.instructions.MoveInstruction;
import dk.tij.rm.instructions.MultiplicationInstruction;
import dk.tij.rm.instructions.OutputInstruction;
import dk.tij.rm.instructions.SubtractionInstruction;

/**
 * @since 1.1.0
 * @author TiJ
 */
public class RegistermaschinePlugin implements IPlugin {
    private PluginContext context;

    public RegistermaschinePlugin() {}

    @Override
    public void onLoad() {
        log("Loading");
    }

    @Override
    public void onEnable(PluginContext context) {
        this.context = context;
        log("Enabling instructions...");

        /**
         * @TODO Add Config Migration
         * The old instruction set is not working, therefore the usage of those instruction handlers
         * will not work anymore. Add migration to transition from package *.registermaschine.* to *.rm.*
         */

        registerInstruction(AdditionInstruction.class);
        registerInstruction(SubtractionInstruction.class);
        registerInstruction(MultiplicationInstruction.class);
        registerInstruction(DivisionInstruction.class);
        registerInstruction(InputInstruction.class);
        registerInstruction(OutputInstruction.class);
        registerInstruction(HaltInstruction.class);
        registerInstruction(MoveInstruction.class);
        registerInstruction(JumpInstruction.class);
    }

    @Override
    public void onDisable() {
        log("Disabling");
    }

    private void registerInstruction(Class<? extends AbstractInstruction> clazz) {
        log("Registering " + clazz.getName());
        context.instructionRegistry().register(clazz);
    }

    private static void log(String msg) {
        System.out.printf("[%s] %s%n", RegistermaschinePlugin.class.getName(), msg);
    }
}
