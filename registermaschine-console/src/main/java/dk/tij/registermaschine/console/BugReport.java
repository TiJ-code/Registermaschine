package dk.tij.registermaschine.console;

import dk.tij.jissuesystem.api.Issue;
import dk.tij.jissuesystem.api.Label;
import dk.tij.jissuesystem.core.IssueReporter;
import dk.tij.jissuesystem.core.LabelContract;
import dk.tij.jissuesystem.provider.IssueProviderType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

public final class BugReport {
    private static final Set<String> REQUIRED_STR_LABELS = Set.of("bug", "CONSOLE");
    private static final Set<Label> REQUIRED_LABELS = REQUIRED_STR_LABELS.stream().map(Label::new).collect(Collectors.toSet());
    private static final BugReport INSTANCE = new BugReport();

    private final IssueReporter reporter;

    private BugReport() {
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

        this.reporter = IssueReporter.builder()
                .provider(IssueProviderType.GITHUB, "TiJ-code", "Registermaschine-Feedback", pat)
                .contract(new LabelContract(Set.of()))
                .build();

        reporter.initialise().join();
    }

    public static boolean report(String title, String description) {
        return INSTANCE.reportIssue(title, description);
    }

    private boolean reportIssue(String title, String description) {
        Issue issue = new Issue.Builder()
                .title(title)
                .body(description)
                .labels(REQUIRED_LABELS)
                .build();

        return reporter.report(issue)
                .thenApply(res -> res.statusCode() == 200 || res.statusCode() == 204)
                .exceptionally(_ -> false)
                .join();
    }
}
