package dk.tij.registermaschine;

import dk.tij.registermaschine.parser.Instruction;
import javafx.application.Platform;
import javafx.concurrent.Task;
import netscape.javascript.JSObject;
import org.fxmisc.richtext.CodeArea;

import java.util.Collections;
import java.util.List;

public class CPU {
    private static final int MAX_JUMP_DEPTH = 32;

    private final int[] registers = new int[16];
    private final boolean[] changed = new boolean[16];
    private final Object inputLock = new Object();
    private Integer pendingInput = null;
    private final JSObject window;
    private final CodeArea codeArea;

    private int currentInputRegister = -1;
    private boolean initial;
    private int programCounter = 0;
    private int jumpDepth = 0;

    private int lastHighlightedLine = -1;
    private volatile boolean isRunning = false;

    public CPU(JSObject window, CodeArea codeArea) {
        this.window = window;
        this.codeArea = codeArea;
        this.initial = true;
        updateRegisterUI();
    }

    private int waitForInput() throws InterruptedException {
        synchronized (inputLock) {
            while (pendingInput == null) {
                inputLock.wait();
            }
            int input = pendingInput;
            pendingInput = null;
            return input;
        }
    }

    public void sendInput(String inputValue) {
        try {
            int input = Integer.parseInt(inputValue);
            if (currentInputRegister >= 0 && currentInputRegister < registers.length) {
                registers[currentInputRegister] = input;
                int reg = currentInputRegister;
                Platform.runLater(() -> window.call("updateRegister", reg, input));
            }
            synchronized (inputLock) {
                pendingInput = input;
                inputLock.notify();
            }
        } catch (NumberFormatException e) {
            Platform.runLater(() -> window.call("logWarning", "Invalid input. Try again."));
            System.err.println("Invalid input: " + inputValue);
        }
    }

    private void updateRegisterUI() {
        Platform.runLater(() -> {
            for (int i = 0; i < registers.length; i++) {
                if (changed[i] || initial) {
                    window.eval(String.format("updateRegister(%d, %d)", i, registers[i]));
                    window.call("blinkRegister", i);
                    changed[i] = false;
                }
            }
            if (initial) {
                window.call("setInitialMaxJumpDepth", MAX_JUMP_DEPTH);
            }
            window.call("updateJumpDepth", jumpDepth);
            initial = false;
        });
    }

    public void executeCode(List<Instruction> instructions) {
        if (isRunning) {
            isRunning = false; // Terminate if already running
            return;
        }
        isRunning = true;

        Platform.runLater(() -> codeArea.setDisable(true)); // Disable editing

        Task<Void> simulationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                programCounter = 0;
                jumpDepth = 0;
                final int[] prevLine = new int[]{-1};

                while (programCounter < instructions.size() && isRunning) {
                    if (jumpDepth >= MAX_JUMP_DEPTH) {
                        Platform.runLater(() -> {
                            window.call("logError", "Exceed maximum jump depth.");
                        });
                        endExecution();
                        break;
                    }

                    final int currentLine = programCounter;
                    Platform.runLater(() -> {
                        if (prevLine[0] != -1) {
                            codeArea.setParagraphStyle(prevLine[0], Collections.emptyList());
                        }
                        codeArea.setParagraphStyle(currentLine, Collections.singletonList("executing-line"));
                        prevLine[0] = currentLine;
                        codeArea.deselect();
                    });

                    lastHighlightedLine = currentLine;

                    dk.tij.registermaschine.parser.Instruction instruction = instructions.get(programCounter);
                    int opcode = instruction.opcode();
                    int arg = instruction.argument();

                    System.out.println(opcode);

                    switch (opcode) {
                        case 0x05:
                            registers[0] = registers[arg];
                            changed[0] = true;
                            break;
                        case 0x07:
                            registers[arg] = registers[0];
                            changed[arg] = true;
                            break;
                        case 0x06:
                            registers[0] = arg;
                            changed[0] = true;
                            break;
                        case 0x01:
                            registers[0] += registers[arg];
                            changed[0] = true;
                            break;
                        case 0x02:
                            registers[0] -= registers[arg];
                            changed[0] = true;
                            break;
                        case 0x03:
                            registers[0] *= registers[arg];
                            changed[0] = true;
                            break;
                        case 0x04:
                            registers[0] /= registers[arg];
                            changed[0] = true;
                            break;
                        case 0x0B:
                            if (!performJump(arg)) return null;
                            break;
                        case 0x0C:
                            if (registers[0] == 0 && !performJump(arg)) return null;
                            break;
                        case 0x0D:
                            if (registers[0] != 0 && !performJump(arg)) return null;
                            break;
                        case 0x0E:
                            if (registers[0] < 0 && !performJump(arg)) return null;
                            break;
                        case 0x0F:
                            if (registers[0] <= 0 && !performJump(arg)) return null;
                            break;
                        case 0x10:
                            if (registers[0] > 0 && !performJump(arg)) return null;
                            break;
                        case 0x11:
                            if (registers[0] >= 0 && !performJump(arg)) return null;
                            break;
                        case 0x08:
                            Platform.runLater(() ->
                                    window.call("toggleSendButton", false)
                            );
                            currentInputRegister = arg;
                            int inputValue = waitForInput();
                            registers[currentInputRegister] = inputValue;
                            changed[currentInputRegister] = true;
                            Platform.runLater(() ->
                                    window.call("toggleSendButton", true)
                            );
                            break;
                        case 0x09:
                            int outputValue = registers[arg];
                            Platform.runLater(() -> {
                                window.call("outputValue", outputValue);
                                window.call("blinkOutput");
                            });
                            break;
                        case 0x0A:
                            Platform.runLater(() -> {
                                window.call("log", "Execution halted.");
                            });
                            endExecution();
                            return null;
                        default:
                            Platform.runLater(() -> {
                                window.call("logError", String.format("Unknown opcode: %d", opcode));
                            });
                            break;
                    }

                    updateRegisterUI();
                    Thread.sleep(1000);
                    programCounter++;
                }
                endExecution();
                return null;
            }
        };

        Thread simulationThread = new Thread(simulationTask);
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    private boolean performJump(int target) {
        jumpDepth++;
        programCounter = target - 2;
        return true;
    }

    public void endExecution() {
        isRunning = false;
        Platform.runLater(() -> {
            codeArea.setDisable(false); // Re-enable editing
            if (lastHighlightedLine != -1) {
                codeArea.setParagraphStyle(lastHighlightedLine, Collections.emptyList());
            }
            window.eval("toggleRunCodeButton(null, false)");
            window.call("log", "Execution halted.");
        });
    }
}