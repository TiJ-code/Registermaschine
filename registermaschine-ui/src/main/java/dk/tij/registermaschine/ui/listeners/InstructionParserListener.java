package dk.tij.registermaschine.ui.listeners;

import dk.tij.registermaschine.core.config.ConfigInstruction;
import dk.tij.registermaschine.core.config.api.IConfigEventListener;
import dk.tij.registermaschine.core.config.api.ParsingEvent;

public class InstructionParserListener implements IConfigEventListener {
    @Override
    public void onElementParsed(ParsingEvent<?> event) {
        if (event.result() instanceof ConfigInstruction instr) {
            System.out.println(instr.mnemonic());
        }
    }
}
