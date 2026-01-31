package dk.tij.registermaschine.core;

import dk.tij.registermaschine.core.config.InstructionRegistry;
import dk.tij.registermaschine.core.instructions.CompiledInstruction;

import java.util.List;

public class Executor {

    private final ExecutionContext context;
    private final InstructionRegistry registry;
    private final List<CompiledInstruction> program;

    public Executor(ExecutionContext context, InstructionRegistry registry, List<CompiledInstruction> program) {
        this.context = context;
        this.registry = registry;
        this.program = program;
    }

    public void run() {
        while (context.getProgrammeCounter() < program.size()) {
            int pc = context.getProgrammeCounter();
            CompiledInstruction instr = program.get(pc);

            context.setProgrammeCounter( pc + 1 );

            registry.getHandler(instr.opcode())
                    .executeInstruction(context, instr.operands());
        }
    }
}
