import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class SortingUI extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(13,  17,  23);   // GitHub dark bg
    private static final Color BG_PANEL     = new Color(22,  27,  34);   // card bg
    private static final Color BG_SIDEBAR   = new Color(18,  22,  29);
    private static final Color BORDER_COLOR = new Color(48,  54,  61);
    private static final Color ACCENT_BLUE  = new Color(88, 166, 255);   // links/highlights
    private static final Color ACCENT_GREEN = new Color(63, 185, 80);    // success
    private static final Color ACCENT_AMBER = new Color(255, 193,  7);   // workers
    private static final Color ACCENT_RED   = new Color(248, 81,  73);   // errors
    private static final Color ACCENT_CYAN  = new Color( 86, 211, 211);  // master lines
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_MUTED   = new Color(139, 148, 158);
    private static final Color TEXT_DIM     = new Color( 88,  96, 105);

    private static final Font FONT_UI     = new Font("Segoe UI",   Font.PLAIN,  13);
    private static final Font FONT_BOLD   = new Font("Segoe UI",   Font.BOLD,   13);
    private static final Font FONT_TITLE  = new Font("Segoe UI",   Font.BOLD,   22);
    private static final Font FONT_MONO   = new Font("JetBrains Mono", Font.PLAIN, 13) ;
    private static final Font FONT_STAT   = new Font("Segoe UI",   Font.BOLD,   28);
    private static final Font FONT_SMALL  = new Font("Segoe UI",   Font.PLAIN,  11);

    // ── Input controls ────────────────────────────────────────────────────────
    private JTextField nField;
    private JTextField kField;
    private JButton    runBtn;
    private JButton    clearBtn;

    // ── Stats cards ──────────────────────────────────────────────────────────
    private JLabel statRunsValue;
    private JLabel statGenValue;
    private JLabel statMergedValue;
    private JLabel statMatchValue;

    // ── Terminal output ───────────────────────────────────────────────────────
    private JTextPane  terminal;
    private StyledDocument termDoc;

    // ── History list ─────────────────────────────────────────────────────────
    private DefaultListModel<String> historyModel;
    private JList<String>            historyList;
    private List<String>             historyDetails = new ArrayList<>();

    // ── Runtime state ─────────────────────────────────────────────────────────
    private int     runCount    = 0;
    private boolean isRunning   = false;
    private Process childProcess = null;

    // ── Classpath for child process ───────────────────────────────────────────
    private String classPath;

    // ─────────────────────────────────────────────────────────────────────────
    public SortingUI() {
        detectClassPath();
        buildUI();
        setVisible(true);
    }

    /** Detect the classpath by looking next to SortingUI.class */
    private void detectClassPath() {
        // Try to find where the .class files are located
        // Check common locations
        String[] candidates = {
            ".",
            System.getProperty("user.dir"),
            System.getProperty("user.dir") + "/bin"
        };

        // Also check gridsim jar
        String[] jarCandidates = {
            "gridsim.jar",
            "../gridsim.jar",
            "lib/gridsim.jar",
            System.getProperty("user.dir") + "/gridsim.jar",
            System.getProperty("user.dir") + "/../gridsim.jar"
        };

        StringBuilder cp = new StringBuilder();

        // Find the directory containing our .class files
        String classDir = ".";
        for (String c : candidates) {
            File f = new File(c, "MainSimulation.class");
            if (f.exists()) {
                classDir = c;
                break;
            }
            // Also check if SortingUI itself is here
            File f2 = new File(c, "SortingUI.class");
            if (f2.exists()) {
                classDir = c;
                break;
            }
        }
        cp.append(classDir);

        // Find gridsim jar
        for (String jc : jarCandidates) {
            File jf = new File(jc);
            if (jf.exists()) {
                cp.append(File.pathSeparator).append(jf.getAbsolutePath());
                break;
            }
        }

        // Also include current classpath
        String existing = System.getProperty("java.class.path");
        if (existing != null && !existing.isEmpty()) {
            cp.append(File.pathSeparator).append(existing);
        }

        classPath = cp.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI CONSTRUCTION
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("Distributed Sorting — GridSim Dashboard");
        setSize(1180, 740);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildSidebar(),   BorderLayout.WEST);
        root.add(buildCenter(),    BorderLayout.CENTER);
    }

    // ── TOP BAR ──────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 24, 12, 24)
        ));

        // Left: logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel icon  = new JLabel("⬡");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        icon.setForeground(ACCENT_BLUE);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Distributed Sorting Simulation");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("GridSim · Master–Worker Architecture");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(sub);

        left.add(icon);
        left.add(titleBox);

        // Right: stat chips
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chips.setOpaque(false);
        chips.add(buildChip("Bandwidth", "1 Mbps",  ACCENT_BLUE));
        chips.add(buildChip("Delay",     "10 ms",   ACCENT_CYAN));
        chips.add(buildChip("MTU",       "1500 B",  ACCENT_AMBER));
        chips.add(buildChip("Topology",  "Star",    ACCENT_GREEN));

        bar.add(left,  BorderLayout.WEST);
        bar.add(chips, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildChip(String label, String value, Color color) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        chip.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
        chip.setBorder(new CompoundBorder(
            new LineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80), 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));
        JLabel lbl = new JLabel(label + ": " + value);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(color);
        chip.add(lbl);
        return chip;
    }

    // ── SIDEBAR ──────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, BORDER_COLOR),
            new EmptyBorder(20, 16, 20, 16)
        ));
        sidebar.setPreferredSize(new Dimension(260, 0));

        // ── Parameters ──
        sidebar.add(sidebarSection("PARAMETERS"));
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(fieldLabel("Elements  (N)"));
        nField = styledField("40");
        sidebar.add(nField);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(fieldLabel("Workers  (K)"));
        kField = styledField("4");
        sidebar.add(kField);
        sidebar.add(Box.createVerticalStrut(20));

        // ── Run button ──
        runBtn = new JButton("▶  Run Simulation");
        runBtn.setFont(FONT_BOLD);
        runBtn.setForeground(Color.WHITE);
        runBtn.setBackground(ACCENT_GREEN);
        runBtn.setFocusPainted(false);
        runBtn.setBorderPainted(false);
        runBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        runBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        runBtn.addActionListener(e -> onRun());
        addHoverEffect(runBtn, ACCENT_GREEN, new Color(45, 160, 60));
        sidebar.add(runBtn);
        sidebar.add(Box.createVerticalStrut(8));

        clearBtn = new JButton("✕  Clear Output");
        clearBtn.setFont(FONT_UI);
        clearBtn.setForeground(TEXT_MUTED);
        clearBtn.setBackground(BG_PANEL);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        clearBtn.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        clearBtn.addActionListener(e -> onClear());
        sidebar.add(clearBtn);

        sidebar.add(Box.createVerticalStrut(28));
        sidebar.add(sidebarDivider());
        sidebar.add(Box.createVerticalStrut(18));

        // ── Run History ──
        sidebar.add(sidebarSection("RUN HISTORY"));
        sidebar.add(Box.createVerticalStrut(10));

        historyModel = new DefaultListModel<>();
        historyList  = new JList<>(historyModel);
        historyList.setFont(FONT_SMALL);
        historyList.setBackground(BG_DARK);
        historyList.setForeground(TEXT_MUTED);
        historyList.setSelectionBackground(new Color(33, 38, 45));
        historyList.setSelectionForeground(ACCENT_BLUE);
        historyList.setBorder(new EmptyBorder(4, 6, 4, 6));
        historyList.setFixedCellHeight(26);
        historyList.setCellRenderer(new HistoryCellRenderer());

        // Click history item to show its stats
        historyList.addListSelectionListener(e -> {
            int idx = historyList.getSelectedIndex();
            if (!e.getValueIsAdjusting() && idx >= 0 && idx < historyDetails.size()) {
                appendToTerminal("\n── History entry #" + (idx + 1) + " ──\n", TEXT_DIM);
                appendToTerminal(historyDetails.get(idx) + "\n", TEXT_MUTED);
            }
        });

        JScrollPane histScroll = new JScrollPane(historyList);
        histScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        histScroll.setBackground(BG_DARK);
        histScroll.getViewport().setBackground(BG_DARK);
        histScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        histScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        histScroll.setPreferredSize(new Dimension(0, 160));
        sidebar.add(histScroll);

        sidebar.add(Box.createVerticalGlue());

        // ── Legend ──
        sidebar.add(sidebarDivider());
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(legendItem("●  Master lines",  ACCENT_CYAN));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(legendItem("●  Worker lines",  ACCENT_AMBER));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(legendItem("●  Merged result", ACCENT_GREEN));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(legendItem("●  Errors",        ACCENT_RED));

        return sidebar;
    }

    // ── CENTER PANEL ─────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(BG_DARK);

        // ── Stats row ──
        center.add(buildStatsRow(), BorderLayout.NORTH);

        // ── Terminal ──
        terminal = new JTextPane();
        terminal.setEditable(false);
        terminal.setBackground(BG_DARK);
        terminal.setCaretColor(ACCENT_BLUE);
        terminal.setBorder(new EmptyBorder(14, 18, 14, 18));
        terminal.setFont(FONT_MONO);

        termDoc = terminal.getStyledDocument();
        addStyle("default", TEXT_PRIMARY);
        addStyle("master",  ACCENT_CYAN);
        addStyle("worker",  ACCENT_AMBER);
        addStyle("merged",  ACCENT_GREEN);
        addStyle("error",   ACCENT_RED);
        addStyle("dim",     TEXT_DIM);
        addStyle("muted",   TEXT_MUTED);
        addStyle("header",  ACCENT_BLUE);

        JScrollPane scroll = new JScrollPane(terminal);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        center.add(scroll, BorderLayout.CENTER);

        // Welcome message
        appendToTerminal("  Distributed Sorting Simulation — GridSim\n", ACCENT_BLUE);
        appendToTerminal("  ─────────────────────────────────────────\n", TEXT_DIM);
        appendToTerminal("  Configure N and K in the sidebar, then press  ▶ Run Simulation.\n", TEXT_MUTED);
        appendToTerminal("  Each run spawns a fresh subprocess — you can run as many times as you like.\n\n", TEXT_MUTED);

        return center;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 1, 0));
        row.setBackground(BORDER_COLOR);
        row.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        statRunsValue   = new JLabel("0");
        statGenValue    = new JLabel("—");
        statMergedValue = new JLabel("—");
        statMatchValue  = new JLabel("—");

        row.add(statCard("Total Runs",       statRunsValue,   ACCENT_BLUE));
        row.add(statCard("Generated (N)",    statGenValue,    ACCENT_CYAN));
        row.add(statCard("Merged Items",     statMergedValue, ACCENT_GREEN));
        row.add(statCard("Sort Accuracy",    statMatchValue,  ACCENT_AMBER));

        return row;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_PANEL);
        card.setBorder(new EmptyBorder(16, 24, 16, 24));

        valueLabel.setFont(FONT_STAT);
        valueLabel.setForeground(accent);

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_DIM);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lbl,        BorderLayout.SOUTH);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS: sidebar components
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel sidebarSection(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_DIM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JSeparator sidebarDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_UI);
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String def) {
        JTextField f = new JTextField(def);
        f.setFont(new Font("Segoe UI", Font.BOLD, 15));
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_DARK);
        f.setCaretColor(ACCENT_BLUE);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        // Focus highlight
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(ACCENT_BLUE, 1),
                    new EmptyBorder(6, 10, 6, 10)
                ));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(6, 10, 6, 10)
                ));
            }
        });
        return f;
    }

    private JLabel legendItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void addHoverEffect(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { if (btn.isEnabled()) btn.setBackground(normal); }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TERMINAL HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void addStyle(String name, Color color) {
        Style style = terminal.addStyle(name, null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontFamily(style, FONT_MONO.getFamily());
        StyleConstants.setFontSize(style, FONT_MONO.getSize());
    }

    private void appendToTerminal(String text, Color color) {
        // find nearest named style
        String styleName = "default";
        if      (color == ACCENT_CYAN)  styleName = "master";
        else if (color == ACCENT_AMBER) styleName = "worker";
        else if (color == ACCENT_GREEN) styleName = "merged";
        else if (color == ACCENT_RED)   styleName = "error";
        else if (color == TEXT_DIM)     styleName = "dim";
        else if (color == TEXT_MUTED)   styleName = "muted";
        else if (color == ACCENT_BLUE)  styleName = "header";

        final String s = styleName;
        SwingUtilities.invokeLater(() -> {
            try {
                termDoc.insertString(termDoc.getLength(), text, terminal.getStyle(s));
            } catch (BadLocationException ex) { /* ignore */ }
        });
    }

    private void appendLine(String line) {
        // Color-route based on content
        String low = line.toLowerCase();
        if (low.contains("master") && (low.contains("generated") || low.contains("sent to") || low.contains("received from") || low.contains("merged"))) {
            if (low.contains("merged final")) {
                appendToTerminal(line + "\n", ACCENT_GREEN);
            } else {
                appendToTerminal(line + "\n", ACCENT_CYAN);
            }
        } else if (low.contains("worker") && (low.contains("received") || low.contains("sent back"))) {
            appendToTerminal(line + "\n", ACCENT_AMBER);
        } else if (low.contains("error") || low.contains("exception")) {
            appendToTerminal(line + "\n", ACCENT_RED);
        } else if (line.startsWith("__SUMMARY")) {
            // skip internal markers from display
        } else {
            appendToTerminal(line + "\n", TEXT_MUTED);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIMULATION LOGIC — runs in a child process
    // ─────────────────────────────────────────────────────────────────────────

    private void onRun() {
        if (isRunning) return;

        // Parse inputs
        int N, K;
        try {
            N = Integer.parseInt(nField.getText().trim());
            K = Integer.parseInt(kField.getText().trim());
        } catch (NumberFormatException ex) {
            flashError("Input Error: N and K must be valid integers.");
            return;
        }

        if (N <= 0 || K <= 0) {
            flashError("Input Error: N and K must both be > 0.");
            return;
        }
        if (K > N) {
            flashError("Input Error: K (workers) cannot exceed N (elements).");
            return;
        }

        // UI → running state
        isRunning = true;
        runBtn.setEnabled(false);
        runBtn.setText("⏳  Running...");
        runBtn.setBackground(ACCENT_AMBER);

        final int fN = N, fK = K;

        // Separator in terminal
        appendToTerminal("\n", TEXT_DIM);
        appendToTerminal(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n",
            TEXT_DIM
        );
        appendToTerminal(
            String.format("  RUN #%d   N=%d   K=%d workers\n", runCount + 1, fN, fK),
            ACCENT_BLUE
        );
        appendToTerminal(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n",
            TEXT_DIM
        );

        new Thread(() -> {
            Map<String, String> summary = new HashMap<>();
            StringBuilder rawLog       = new StringBuilder();
            boolean parseMode          = false;

            try {
                // Build child process command
                String javaExe = ProcessHandle.current().info().command()
                                              .orElse("java");

                ProcessBuilder pb = new ProcessBuilder(
                    javaExe, "-cp", classPath,
                    "SimulationRunner",
                    String.valueOf(fN), String.valueOf(fK)
                );
                pb.redirectErrorStream(true);   // merge stderr into stdout
                childProcess = pb.start();

                // Stream output line by line
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(childProcess.getInputStream()))) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        rawLog.append(line).append("\n");

                        if (line.equals("__SUMMARY_START__")) {
                            parseMode = true;
                            continue;
                        }
                        if (line.equals("__SUMMARY_END__")) {
                            parseMode = false;
                            continue;
                        }

                        if (parseMode) {
                            int eq = line.indexOf('=');
                            if (eq > 0) {
                                summary.put(line.substring(0, eq),
                                            line.substring(eq + 1));
                            }
                        } else {
                            final String display = line;
                            appendLine(display);
                        }
                    }
                }

                childProcess.waitFor();

            } catch (Exception ex) {
                appendToTerminal("ERROR: " + ex.getMessage() + "\n", ACCENT_RED);
            }

            // ── Post-run: update stats on EDT ──
            final Map<String, String> fin = summary;
            final String logSnapshot      = rawLog.toString();

            SwingUtilities.invokeLater(() -> {
                runCount++;

                int genSize    = parseIntSafe(fin.get("GENERATED_SIZE"), fN);
                int finalSize  = parseIntSafe(fin.get("FINAL_SIZE"),     0);
                int reportedN  = parseIntSafe(fin.get("N"), fN);
                int reportedK  = parseIntSafe(fin.get("K"), fK);

                // Sort accuracy: finalSize should equal generatedSize (N)
                // and the merged list should be sorted (we verify client-side)
                double matchPct = (genSize > 0)
                    ? (Math.min(finalSize, genSize) * 100.0 / genSize)
                    : 0.0;

                // If we got the actual lists, verify sort correctness
                String finalListStr = fin.get("FINAL_LIST");
                if (finalListStr != null && !finalListStr.isEmpty() && genSize > 0) {
                    List<Integer> finalList = parseIntList(finalListStr);
                    boolean isSorted = isSortedList(finalList);
                    if (isSorted && finalList.size() == genSize) {
                        matchPct = 100.0;
                    } else if (!isSorted) {
                        matchPct = 0.0;
                    }
                }

                String pctStr = (matchPct == 100.0) ? "100%" :
                                new DecimalFormat("0.0").format(matchPct) + "%";

                // Update stat cards
                statRunsValue.setText(String.valueOf(runCount));
                statGenValue.setText(String.valueOf(reportedN));
                statMergedValue.setText(String.valueOf(finalSize));
                statMatchValue.setText(pctStr);
                statMatchValue.setForeground(matchPct == 100.0 ? ACCENT_GREEN : ACCENT_RED);

                // Summary block in terminal
                appendToTerminal("\n", TEXT_DIM);
                appendToTerminal("  ── Summary ──────────────────────────────────\n", TEXT_DIM);
                appendToTerminal(String.format("  N = %d   K = %d workers\n", reportedN, reportedK), TEXT_MUTED);
                appendToTerminal(String.format("  Generated : %d items\n", genSize),    TEXT_MUTED);
                appendToTerminal(String.format("  Merged    : %d items\n", finalSize),  TEXT_MUTED);
                appendToTerminal(String.format("  Coverage  : %.1f%%  (%d / %d)\n",
                    (genSize > 0 ? finalSize * 100.0 / genSize : 0.0), finalSize, genSize), TEXT_MUTED);
                appendToTerminal(String.format("  Accuracy  : %s\n", pctStr),
                    matchPct == 100.0 ? ACCENT_GREEN : ACCENT_RED);
                appendToTerminal("  ─────────────────────────────────────────────\n\n", TEXT_DIM);

                // Add to history
                String histLabel = String.format("#%d  N=%d  K=%d  → %s", runCount, reportedN, reportedK, pctStr);
                String histDetail = String.format(
                    "Run #%d\n  N=%d  K=%d  Generated=%d  Merged=%d  Accuracy=%s",
                    runCount, reportedN, reportedK, genSize, finalSize, pctStr
                );
                historyModel.addElement(histLabel);
                historyDetails.add(histDetail);
                historyList.ensureIndexIsVisible(historyModel.size() - 1);

                // Scroll terminal to bottom
                terminal.setCaretPosition(termDoc.getLength());

                // Restore UI
                isRunning = false;
                runBtn.setEnabled(true);
                runBtn.setText("▶  Run Simulation");
                runBtn.setBackground(ACCENT_GREEN);
                childProcess = null;
            });
        }).start();
    }

    private void onClear() {
        try {
            termDoc.remove(0, termDoc.getLength());
        } catch (BadLocationException ex) { /* ignore */ }
        appendToTerminal("  Output cleared. Ready for next run.\n\n", TEXT_DIM);
    }

    private void flashError(String msg) {
        appendToTerminal("  ✖  " + msg + "\n", ACCENT_RED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITIES
    // ─────────────────────────────────────────────────────────────────────────

    private static int parseIntSafe(String s, int fallback) {
        if (s == null) return fallback;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    /** Parse "[1, 2, 3]" style string into a List<Integer>. */
    private static List<Integer> parseIntList(String s) {
        List<Integer> result = new ArrayList<>();
        if (s == null) return result;
        s = s.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]"))   s = s.substring(0, s.length() - 1);
        for (String tok : s.split(",")) {
            tok = tok.trim();
            if (!tok.isEmpty()) {
                try { result.add(Integer.parseInt(tok)); }
                catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }

    private static boolean isSortedList(List<Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) < list.get(i - 1)) return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HISTORY CELL RENDERER
    // ─────────────────────────────────────────────────────────────────────────

    private class HistoryCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setFont(FONT_SMALL);
            label.setBackground(isSelected ? new Color(33, 38, 45) : BG_DARK);
            label.setForeground(isSelected ? ACCENT_BLUE : TEXT_MUTED);
            label.setBorder(new EmptyBorder(3, 6, 3, 6));
            return label;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("ScrollBar.thumb",            new Color(48, 54, 61));
        UIManager.put("ScrollBar.thumbHighlight",   new Color(60, 68, 77));
        UIManager.put("ScrollBar.background",       BG_DARK);
        UIManager.put("ScrollBar.trackHighlight",   BG_DARK);

        SwingUtilities.invokeLater(SortingUI::new);
    }
}
