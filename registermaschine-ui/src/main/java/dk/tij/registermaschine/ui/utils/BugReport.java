package dk.tij.registermaschine.ui.utils;

import dk.tij.jissuesystem.JIssueSystem;

import java.util.concurrent.CompletableFuture;

public final class BugReport {
    private BugReport() {}

    private static final JIssueSystem issueSystem;

    static {
        String pat = null;
        try {
            var is = BugReport.class.getResourceAsStream("/.env");
            if (is != null) {
                pat = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
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
