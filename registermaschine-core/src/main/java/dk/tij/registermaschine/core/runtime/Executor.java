package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.compilation.CompiledProgram;
import dk.tij.registermaschine.core.config.InstructionSet;
import dk.tij.registermaschine.core.instructions.AbstractInstruction;
import dk.tij.registermaschine.core.compilation.compiling.CompiledInstruction;

public class Executor {
    private final ExecutionContext context;
    private final InstructionSet instructionSet;
    private CompiledProgram program;

    private boolean running;

    public Executor(ExecutionContext context, InstructionSet instructionSet) {
        this.context = context;
        this.instructionSet = instructionSet;
        this.running = false;
    }

    public Executor(ExecutionContext context, InstructionSet instructionSet, CompiledProgram program) {
        this(context, instructionSet);
        this.program = program;
    }

    public void run() {
        if (running) return;

        running = true;
        context.startExecution();

        while (!context.isHalted() && context.getProgrammeCounter() < program.size()) {
            int pc = context.getProgrammeCounter();
            CompiledInstruction instr = program.get(pc);

            context.setProgrammeCounter( pc + 1 );

            AbstractInstruction handler = instructionSet.getHandler(instr.opcode());
            if (handler.shouldExecute(context))
                handler.executeInstruction(context, instr.operands());
        }

        running = false;
    }

    public void setProgram(CompiledProgram program) {
        if (running) return;
        this.program = program;
    }
}
