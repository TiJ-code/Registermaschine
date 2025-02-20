package dk.tij.registermaschine.editor;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SyntaxHighlighter {
    private static final Pattern SYNTAX_PATTERN;

    static {
        String OPCODE_PATTERN = "(?i)\\b(ADD|SUB|MUL|DIV|LDA|LDK|STA|HLT)\\b";
        String JMP_PATTERN = "(?i)\\b(JMP|JEZ|JNE|JLZ|JLE|JGZ|JGE)\\b";
        String UTIL_PATTERN = "(?i)\\b(OUT|INP)\\b";
        String REGISTER_PATTERN = "\\$0[1-9A-Fa-f]";
        String CODE_LINE_PATTERN = "\\b0x[0-9A-Fa-f]+\\b";

        SYNTAX_PATTERN = Pattern.compile(
                "(?<OPCODE>" + OPCODE_PATTERN + ")"
                        + "|(?<REGISTER>" + REGISTER_PATTERN + ")"
                        + "|(?<CODELINE>" + CODE_LINE_PATTERN + ")"
                        + "|(?<JMP>" + JMP_PATTERN + ")"
                        + "|(?<UTIL>" + UTIL_PATTERN + ")"
        );
    }

    public static void applyHighlighting(CodeArea codeArea) {
        codeArea.textProperty().addListener((observer, oldValue, newValue) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newValue));
        });
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = SYNTAX_PATTERN.matcher(text);
        int lastMatchEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("OPCODE") != null ? "opcode" :
                            matcher.group("REGISTER") != null ? "register" :
                                    matcher.group("JMP") != null ? "jmp" :
                                            matcher.group("UTIL") != null ? "util" :
                                                    matcher.group("CODELINE") != null ? "codeline" : null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastMatchEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastMatchEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastMatchEnd);
        return spansBuilder.create();
    }
}
