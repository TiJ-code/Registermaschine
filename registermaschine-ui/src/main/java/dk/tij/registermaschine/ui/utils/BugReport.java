package dk.tij.registermaschine.ui.utils;

import dk.tij.jissuesystem.JIssueSystem;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public final class BugReport {
    private BugReport() {}

    private static final JIssueSystem issueSystem;

    static {
        String pat = null;
        try {
            var is = BugReport.class.getResourceAsStream("/bug.env");
            if (is != null) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                content = new StringBuilder(content).reverse().toString();

                pat = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
                is.close();
            }
        } catch (Exception _) {}

        issueSystem = new JIssueSystem("TiJ-code", "Registermaschine-Feedback", pat);
    }

    public static CompletableFuture<Boolean> report(String title, String description) {
        return issueSystem.report(title, description)
                .thenApply(status -> status == 204 || status == 200)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                });
    }
}
