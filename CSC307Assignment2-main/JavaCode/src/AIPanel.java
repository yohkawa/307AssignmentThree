import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * UI area reserved for Groq-powered story assistance.
 *
 * This class does not call Groq directly. It sends Groq connection and AI review requests to
 * {@link AppController}, so another teammate can provide the real API implementation.
 *
 *  @author Eman Castilo Hernandez
 *  @version 1.0
 */
public final class AIPanel extends JPanel {

    private final Supplier<AppController> controllerSupplier;
    private final Supplier<Story> selectedStorySupplier;
    private final Consumer<String> statusUpdater;

    private final JTextArea promptArea = new JTextArea();
    private final JTextArea resultArea = new JTextArea();

    private final JButton connectGroqButton = new JButton("Connect to Groq");
    private final JButton reviewButton = new JButton("Review Selected Story");

    public AIPanel(
            Supplier<AppController> controllerSupplier,
            Supplier<Story> selectedStorySupplier,
            Consumer<String> statusUpdater
    ) {
        super(new BorderLayout(4, 4));

        this.controllerSupplier = Objects.requireNonNull(controllerSupplier, "controllerSupplier");
        this.selectedStorySupplier = Objects.requireNonNull(selectedStorySupplier, "selectedStorySupplier");
        this.statusUpdater = Objects.requireNonNull(statusUpdater, "statusUpdater");

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        configureTextAreas();

        connectGroqButton.addActionListener(e -> runGroqConnection());
        reviewButton.addActionListener(e -> runReview());

        add(createPromptSection(), BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        updateSelection(null);
    }

    public void updateSelection(Story selectedStory) {
        reviewButton.setEnabled(selectedStory != null);
    }

    private void configureTextAreas() {
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setRows(5);
        promptArea.setText("Review this story for INVEST quality and suggest clearer acceptance criteria.");

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
    }

    private JPanel createPromptSection() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        panel.add(new JLabel("Prompt for selected story:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(promptArea), BorderLayout.CENTER);
        panel.add(createButtonRow(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonRow() {
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 4, 4));

        buttonRow.add(connectGroqButton);
        buttonRow.add(reviewButton);

        return buttonRow;
    }

    private void runGroqConnection() {
        try {
            controllerSupplier.get().connectToGroq(this);
            statusUpdater.accept("Groq connection action completed.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Groq connection failed",
                    JOptionPane.ERROR_MESSAGE
            );
            statusUpdater.accept("Groq connection failed.");
        }
    }

    private void runReview() {
        try {
            Story selectedStory = selectedStorySupplier.get();
            String prompt = promptArea.getText().trim();

            String result = controllerSupplier.get().reviewStoryWithAI(selectedStory, prompt);

            resultArea.setText(result);
            resultArea.setCaretPosition(0);
            statusUpdater.accept("AI panel action completed.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "AI review failed",
                    JOptionPane.ERROR_MESSAGE
            );
            statusUpdater.accept("AI review failed.");
        }
    }
}