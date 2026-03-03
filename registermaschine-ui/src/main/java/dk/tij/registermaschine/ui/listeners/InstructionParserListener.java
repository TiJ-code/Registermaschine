package dk.tij.registermaschine.ui.listeners;

import dk.tij.registermaschine.core.config.ConfigInstruction;
import dk.tij.registermaschine.core.config.api.IConfigEventListener;
import dk.tij.registermaschine.core.config.api.ParsingEvent;
import dk.tij.registermaschine.ui.UiApplication;

public class InstructionParserListener implements IConfigEventListener {
    @Override
    public void onElementParsed(ParsingEvent<?> event) {
        if (!UiApplication.DEBUG_PARSING) return;

        if (event.result() instanceof ConfigInstruction instr) {
            System.out.println(instr.mnemonic());
        }
    }
}
