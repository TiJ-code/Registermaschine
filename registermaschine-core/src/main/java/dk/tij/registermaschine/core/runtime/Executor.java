package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class Executor {
    private final IExecutionContext context;
    private final IInstructionSet instructionSet;
    private ICompiledProgram program;

    private boolean running;

    public Executor(IExecutionContext context, IInstructionSet instructionSet) {
        this.context = context;
        this.instructionSet = instructionSet;
        this.running = false;
    }

    public Executor(IExecutionContext context, IInstructionSet instructionSet, ICompiledProgram program) {
        this(context, instructionSet);
        this.program = program;
    }

    public void run() {
        if (running) return;

        running = true;
        context.startExecution();

        while (!context.isHalted() && context.getProgrammeCounter() < program.size()) {
            int pc = context.getProgrammeCounter();
            ICompiledInstruction instr = program.get(pc);

            context.step();

            AbstractInstruction handler = instructionSet.getHandler(instr.opcode());
            if (handler.shouldExecute(context))
                handler.executeInstruction(context, instr.operands());
        }

        running = false;
    }

    public void setProgram(ICompiledProgram program) {
        if (running) return;
        this.program = program;
    }
}
