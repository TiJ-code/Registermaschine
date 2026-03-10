package dk.tij.registermaschine.core.runtime;

import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledInstruction;
import dk.tij.registermaschine.core.compilation.api.compiling.ICompiledProgram;
import dk.tij.registermaschine.core.instructions.api.AbstractInstruction;
import dk.tij.registermaschine.core.instructions.api.IInstructionSet;
import dk.tij.registermaschine.core.runtime.api.IExecutionContext;

/**
 * The {@link Executor} is responsible for executing a compiled program on the
 * Registermaschine runtime environment.
 *
 * <p>It implements the classic <em>fetch-execute cycle</em> by repeatedly fetching
 * instructions from a compiled program and delegating their execution to the
 * corresponding {@link AbstractInstruction} handler obtained from the
 * {@link IInstructionSet}.</p>
 *
 * <p>The executor operates on a separate thread and continues execution until one
 * of the following conditions occurs:</p>
 *
 * <ul>
 *     <li>The execution is manually stopped via {@link #stop()}</li>
 *     <li>The execution context reports that the machine has halted.</li>
 *     <li>The program counter exceeds the program length.</li>
 *     <li>The execution thread is interrupted.</li>
 * </ul>
 *
 * <p>Each instruction cycle follows these steps:</p>
 *
 * <ol>
 *     <li>Retrieve the current instruction using the program counter.</li>
 *     <li>Advance the execution context using {@link IExecutionContext#step()}</li>
 *     <li>Resolve the instruction handler from the {@link IInstructionSet}</li>
 *     <li>Check whether the instruction should execute using
 *     {@link AbstractInstruction#shouldExecute(IExecutionContext)}.</li>
 *     <li>Execute the instruction if permitted.</li>
 * </ol>
 *
 * <p>The executor supports rate limiting by enforcing a minimum delay between
 * instruction cycles. This allows deterministic step timing and prevents
 * excessive CPU usage during continuous execution.</p>
 *
 * <p>The execution context ({@link IExecutionContext}) manages the runtime
 * of the Registermaschine, including the program counter, registers, and
 * state.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class Executor implements Runnable {
    private static final long UNLIMITED_RATE_LIMIT_MS = 10;

    private Thread currentThread;

    private final IExecutionContext context;
    private final IInstructionSet instructionSet;
    private ICompiledProgram program;

    private volatile boolean running;
    private long delayMs = UNLIMITED_RATE_LIMIT_MS;

    /**
     * Creates a new executor for a Registermaschine runtime.
     *
     * @param context the execution context holding the runtime state
     * @param instructionSet the instruction set used to resolve instructions
     */
    public Executor(IExecutionContext context, IInstructionSet instructionSet) {
        this.context = context;
        this.instructionSet = instructionSet;
        this.running = false;
    }

    /**
     * Creates a new executor with a preloaded program.
     *
     * @param context the execution context holding the runtime state
     * @param instructionSet the instruction set used to resolve instructions
     * @param program the compiled program to execute
     */
    public Executor(IExecutionContext context, IInstructionSet instructionSet, ICompiledProgram program) {
        this(context, instructionSet);
        this.program = program;
    }

    /**
     * Starts the execution loop of the Registermaschine.
     *
     * <p>This method performs the continuous instruction execution cycle until the
     * program halts, the executor is stopped, or the thread is interrupted.</p>
     *
     * <p>Execution begins by notifying the {@link IExecutionContext} that execution
     * has started. The executor then repeatedly fetches instructions, resolves the
     * corresponding instruction handler, and executes them if the condition allows it</p>
     *
     * <p>A delay may be introduced between cycles depending on the config and
     * execution speed.</p>
     */
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

    /**
     * Sets the compiled program that should be executed.
     *
     * <p>This method has no effect if the executor is currently running.</p>
     *
     * @param program the compiled program to execute
     */
    public void setProgram(ICompiledProgram program) {
        if (running) return;
        this.program = program;
    }

    /**
     * Sets the execution speed of the Registermaschine.
     *
     * <p>The speed is defined in instructions per second (Hertz). Internally
     * this is converted to a delay between instruction cycles.</p>
     *
     * <p>A minimum delay is enforced to prevent excessively high execution
     * that could lead to unnecessary CPU usage.</p>
     *
     * <p>This method has no effect if the executor is currently running.</p>
     *
     * @param hertz the desired execution frequency in instructions per second
     */
    public void setSpeed(int hertz) {
        if (running) return;
        this.delayMs = Math.max(UNLIMITED_RATE_LIMIT_MS, 1000 / hertz);
    }

    /**
     * Stops the execution loop.
     *
     * <p>This method signals the executor to terminate and interrupts the
     * execution thread if it is currently running.</p>
     */
    public void stop() {
        this.running = false;
        if (currentThread != null)
            currentThread.interrupt();
    }
}
