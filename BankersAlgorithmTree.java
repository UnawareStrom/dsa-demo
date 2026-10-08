import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BankersAlgorithmTree extends JFrame {

    private int numProcesses;
    private int numResources;
    private int[] available;
    private int[][] max;
    private int[][] allocation;
    private int[][] need;
    private JTextArea outputArea;

    public BankersAlgorithmTree() {
        setTitle("Banker's Algorithm State Space Tree");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        inputPanel.add(new JLabel("Number of Processes:"));
        JTextField txtNumProcesses = new JTextField("3");
        inputPanel.add(txtNumProcesses);

        inputPanel.add(new JLabel("Number of Resources:"));
        JTextField txtNumResources = new JTextField("3");
        inputPanel.add(txtNumResources);

        inputPanel.add(new JLabel("Allocation Matrix (space separated rows):"));
        JTextArea txtAllocation = new JTextArea("0 1 0\n2 0 0\n3 0 2");
        inputPanel.add(new JScrollPane(txtAllocation));

        inputPanel.add(new JLabel("Max Matrix (space separated rows):"));
        JTextArea txtMax = new JTextArea("7 5 3\n3 2 2\n9 0 2");
        inputPanel.add(new JScrollPane(txtMax));

        inputPanel.add(new JLabel("Available Resources (space separated):"));
        JTextField txtAvailable = new JTextField("3 3 2");
        inputPanel.add(txtAvailable);

        JButton btnCalculate = new JButton("Calculate Safe Sequences");
        inputPanel.add(new JLabel("")); // Empty cell for alignment
        inputPanel.add(btnCalculate);

        add(inputPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output (State Space Tree)"));
        add(scrollPane, BorderLayout.CENTER);

        btnCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    outputArea.setText(""); // Clear previous output
                    numProcesses = Integer.parseInt(txtNumProcesses.getText().trim());
                    numResources = Integer.parseInt(txtNumResources.getText().trim());
                    
                    available = new int[numResources];
                    max = new int[numProcesses][numResources];
                    allocation = new int[numProcesses][numResources];
                    need = new int[numProcesses][numResources];

                    // Parse available resources
                    Scanner scanAvail = new Scanner(txtAvailable.getText());
                    for (int i = 0; i < numResources; i++) {
                        available[i] = scanAvail.nextInt();
                    }
                    scanAvail.close();

                    // Parse Allocation Matrix
                    Scanner scanAlloc = new Scanner(txtAllocation.getText());
                    for (int i = 0; i < numProcesses; i++) {
                        for (int j = 0; j < numResources; j++) {
                            allocation[i][j] = scanAlloc.nextInt();
                        }
                    }
                    scanAlloc.close();

                    // Parse Max Matrix
                    Scanner scanMax = new Scanner(txtMax.getText());
                    for (int i = 0; i < numProcesses; i++) {
                        for (int j = 0; j < numResources; j++) {
                            max[i][j] = scanMax.nextInt();
                        }
                    }
                    scanMax.close();

                    calculateNeed();
                    findSafeSequences();

                } catch (Exception ex) {
                    outputArea.setText("Error parsing input: " + ex.getMessage() + "\nPlease check that dimensions match the data.");
                }
            }
        });
    }

    private void appendOutput(String text) {
        outputArea.append(text + "\n");
    }

    public void calculateNeed() {
        appendOutput("Need Matrix:");
        for (int i = 0; i < numProcesses; i++) {
            StringBuilder sb = new StringBuilder("P" + i + ": ");
            for (int j = 0; j < numResources; j++) {
                need[i][j] = max[i][j] - allocation[i][j];
                sb.append(need[i][j]).append(" ");
            }
            appendOutput(sb.toString());
        }
    }

    private boolean isSafe(int process, int[] work) {
        for (int i = 0; i < numResources; i++) {
            if (need[process][i] > work[i]) {
                return false;
            }
        }
        return true;
    }

    public void findSafeSequences() {
        boolean[] finish = new boolean[numProcesses];
        int[] work = new int[numResources];
        System.arraycopy(available, 0, work, 0, numResources);
        List<Integer> safeSequence = new ArrayList<>();
        appendOutput("\nState Space Tree Traversal for Safe Sequences:");
        exploreStateSpaceTree(work, finish, safeSequence, 0);
    }

    private void exploreStateSpaceTree(int[] work, boolean[] finish, List<Integer> currentSequence, int depth) {
        if (currentSequence.size() == numProcesses) {
            appendOutput(getIndent(depth) + "Found Safe Sequence: " + currentSequence);
            return;
        }

        boolean foundPath = false;
        for (int i = 0; i < numProcesses; i++) {
            if (!finish[i] && isSafe(i, work)) {
                // Print the edge in the state space tree
                appendOutput(getIndent(depth) + "-> Try Process P" + i);

                // Simulate resource allocation
                int[] newWork = new int[numResources];
                System.arraycopy(work, 0, newWork, 0, numResources);
                for (int j = 0; j < numResources; j++) {
                    newWork[j] += allocation[i][j];
                }
                
                finish[i] = true;
                currentSequence.add(i);

                // Recurse deeper into the tree
                exploreStateSpaceTree(newWork, finish, currentSequence, depth + 1);

                // Backtrack
                finish[i] = false;
                currentSequence.remove(currentSequence.size() - 1);
                
                appendOutput(getIndent(depth) + "<- Backtrack from P" + i);
                foundPath = true;
            }
        }
        
        if (!foundPath && currentSequence.size() > 0 && currentSequence.size() < numProcesses) {
            appendOutput(getIndent(depth) + "Dead end reached (Unsafe state from here).");
        }
    }
    
    private String getIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // Run GUI in the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            BankersAlgorithmTree gui = new BankersAlgorithmTree();
            gui.setLocationRelativeTo(null); // Center the window
            gui.setVisible(true);
        });
    }
}
