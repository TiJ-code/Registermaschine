package dk.tij.registermaschine.core;

import dk.tij.registermaschine.core.config.InstructionRegistry;
import dk.tij.registermaschine.core.instructions.CompiledInstruction;

import java.util.List;

public class Executor {

    private final ExecutionContext context;
    private final InstructionRegistry registry;
    private List<CompiledInstruction> program;

    private boolean running;

    public Executor(ExecutionContext context, InstructionRegistry registry) {
        this.context = context;
        this.registry = registry;
        this.running = false;
    }

    public Executor(ExecutionContext context, InstructionRegistry registry, List<CompiledInstruction> program) {
        this(context, registry);
        this.program = program;
    }

    public void run() {
        if (running) return;

        running = true;
        while (context.getProgrammeCounter() < program.size()) {
            int pc = context.getProgrammeCounter();
            CompiledInstruction instr = program.get(pc);

            context.setProgrammeCounter( pc + 1 );

            registry.getHandler(instr.opcode())
                    .executeInstruction(context, instr.operands());
        }
        running = false;
    }

    public void setProgram(List<CompiledInstruction> program) {
        if (running) return;
        this.program = program;
    }
}
