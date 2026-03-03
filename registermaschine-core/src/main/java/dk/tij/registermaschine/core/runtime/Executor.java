package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

public class Executor implements Runnable {
    private static final long UNLIMITED_RATE_LIMIT_MS = 10;

    private Thread currentThread;

    private final IExecutionContext context;
    private final IInstructionSet instructionSet;
    private ICompiledProgram program;

    private volatile boolean running;
    private long delayMs = UNLIMITED_RATE_LIMIT_MS;

    public Executor(IExecutionContext context, IInstructionSet instructionSet) {
        this.context = context;
        this.instructionSet = instructionSet;
        this.running = false;
    }

    public Executor(IExecutionContext context, IInstructionSet instructionSet, ICompiledProgram program) {
        this(context, instructionSet);
        this.program = program;
    }

    @Override
    public void run() {
        running = true;
        context.startExecution();
        currentThread = Thread.currentThread();

        try {
            while (running && !context.isHalted() && context.getProgrammeCounter() < program.size()) {
                long cycleStart = System.nanoTime();

                int pc = context.getProgrammeCounter();
                ICompiledInstruction instr = program.get(pc);
                context.step();

                AbstractInstruction handler = instructionSet.getHandler(instr.opcode());
                if (handler.shouldExecute(context)) {
                    try {
                        handler.executeInstruction(context, instr.operands());
                    } catch (Exception e) {
                        System.out.println("Execution interrupted during input, stopping!");
                        break;
                    }
                }


                long cycleTimeNs = System.nanoTime() - cycleStart;
                long targetCycleNs = delayMs * 1_000_000L;
                long sleepNs = targetCycleNs - cycleTimeNs;

                if (sleepNs > 0) {
                    Thread.sleep(sleepNs / 1_000_000L, (int)(sleepNs % 1_000_000L));
                }
            }
        } catch (InterruptedException e ) {
            System.err.println("Execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            running = false;
            currentThread = null;
            if (!context.isHalted())
                context.stopExecution();
        }
    }

    public void setProgram(ICompiledProgram program) {
        if (running) return;
        this.program = program;
    }

    public void setSpeed(int hertz) {
        if (running) return;
        this.delayMs = Math.max(UNLIMITED_RATE_LIMIT_MS, 1000 / hertz);
    }

    public void stop() {
        this.running = false;
        if (currentThread != null)
            currentThread.interrupt();
    }
}
