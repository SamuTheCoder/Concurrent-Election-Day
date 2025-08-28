import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
/**
 * ElectionSimulationGUI.java
 * This class represents the graphical user interface for the election simulation.
 * It provides a visual representation of the simulation, including:
 * - The state of voters (waiting, approved, rejected, etc.).
 * - The state of the poll clerk, pollster, and polling station.
 * - The number of voters inside the polling station, processed voters, and remaining voters.
 * - A log area to display simulation events.
 * The GUI is updated in real-time as the simulation progresses.
 */

public class ElectionSimulationGUI extends JFrame {
    private JTextArea logArea;
    private JPanel voterPanel;
    private Map<Integer, JLabel> voterLabels;
    private JLabel pollClerkLabel;
    private JLabel pollsterLabel;
    private JLabel pollingStationLabel;
    private JLabel votersInsideLabel;
    private JLabel votersProcessedLabel;
    private JLabel votersRemainingLabel;
    private JButton stopButton;
    private static final Color COLOR_WAITING = Color.YELLOW;
    private static final Color COLOR_APPROVED = Color.GREEN;
    private static final Color COLOR_VOTED = Color.BLUE;
    private static final Color COLOR_REJECTED = Color.RED;
    private static final Color COLOR_REBORN = new Color(128, 0, 128); // Purple
    private static final Color COLOR_TERMINATED = Color.GRAY;

    /**
     * Constructor for ElectionSimulationGUI.
     * Initializes the GUI components and sets up the layout.
     */
    public ElectionSimulationGUI() {
        setTitle("Election Day Simulation");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Log area to display simulation events
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        // Create pane to divide areas
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(800);
        splitPane.setResizeWeight(0.7);

        //Panel to display voters and status
        JPanel simulationPanel = new JPanel(new BorderLayout());

        // Panel to display voters
        voterPanel = new JPanel();
        voterPanel.setLayout(new GridLayout(0, 2)); // Two columns for voters
        voterLabels = new HashMap<>();

        // Panel to display poll clerk, pollster, and polling station
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(3, 1));

        // Poll Clerk
        pollClerkLabel = new JLabel("Poll Clerk: Idle", new ImageIcon("poll_clerk.png"), SwingConstants.LEFT);
        pollClerkLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        pollClerkLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        // Pollster
        pollsterLabel = new JLabel("Pollster: Idle", new ImageIcon("pollster.png"), SwingConstants.LEFT);
        pollsterLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        pollsterLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        // Polling Station
        pollingStationLabel = new JLabel("Polling Station: Closed", new ImageIcon("polling_station.png"), SwingConstants.LEFT);
        pollingStationLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        pollingStationLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        // Statistics panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(3, 1));
        votersInsideLabel = new JLabel("Voters inside: 0", SwingConstants.LEFT);
        votersProcessedLabel = new JLabel("Voters processed: 0", SwingConstants.LEFT);
        votersRemainingLabel = new JLabel("Voters remaining: 0", SwingConstants.LEFT);

        // Stop button
        stopButton = new JButton("Stop Simulation");
        stopButton.setPreferredSize(new Dimension(100, 30));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
            }
        });
        
        statsPanel.add(votersInsideLabel);
        statsPanel.add(votersProcessedLabel);
        statsPanel.add(votersRemainingLabel);
        statsPanel.add(stopButton);

        statusPanel.add(pollClerkLabel);
        statusPanel.add(pollsterLabel);
        statusPanel.add(pollingStationLabel);

        simulationPanel.add(voterPanel, BorderLayout.CENTER); 
        simulationPanel.add(statusPanel, BorderLayout.EAST); 
        simulationPanel.add(statsPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(simulationPanel);
        splitPane.setRightComponent(scrollPane);

        add(splitPane, BorderLayout.CENTER);

        setVisible(true);

        
    }

    /**
     * Update the state of a voter in the GUI.
     * @param voterId The ID of the voter.
     * @param state The new state of the voter.
     */
    public void updateVoterState(int voterId, String state) {
        SwingUtilities.invokeLater(() -> {
            JLabel voterLabel = voterLabels.get(voterId);
            if (voterLabel == null) {
                voterLabel = new JLabel("Voter " + voterId + ": " + state, new ImageIcon("voter.png"), SwingConstants.LEFT);
                voterLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
                voterLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                voterLabel.setOpaque(true); // Enable background color
                voterLabels.put(voterId, voterLabel);
                voterPanel.add(voterLabel);
            } else {
                voterLabel.setText("Voter " + voterId + ": " + state);
            }
    
            // Set background color based on state
            switch (state) {
                case "Waiting to enter", "Entering polling station" -> voterLabel.setBackground(COLOR_WAITING);
                case "Approved and voting" -> voterLabel.setBackground(COLOR_APPROVED);
                case "Voted and exiting" -> voterLabel.setBackground(COLOR_VOTED);
                case "Rejected by poll clerk and left polling station" -> voterLabel.setBackground(COLOR_REJECTED);
                case "Voter Reborn", "Reborn and re-entering" -> voterLabel.setBackground(COLOR_REBORN);
                case "Terminated" -> voterLabel.setBackground(COLOR_TERMINATED);
                default -> voterLabel.setBackground(Color.WHITE);
            }
    
            voterPanel.revalidate();
            voterPanel.repaint();
        });
    }

    /**
     * Update the state of the poll clerk in the GUI.
     * @param state The new state of the poll clerk.
     */
    public void updatePollClerkState(String state) {
        SwingUtilities.invokeLater(() -> {
            pollClerkLabel.setText("Poll Clerk: " + state);
            // Set background color based on state
            switch (state) {
                case "Processing voters" -> pollClerkLabel.setBackground(Color.GREEN);
                case "Election ended" -> pollClerkLabel.setBackground(Color.RED);
                default -> pollClerkLabel.setBackground(Color.WHITE);
            }
        });
    }

    /**
     * Update the state of the pollster in the GUI.
     * @param state The new state of the pollster.
     */
    public void updatePollsterState(String state) {
        SwingUtilities.invokeLater(() -> {
            pollsterLabel.setText("Pollster: " + state);
            // Set background color based on state
            switch (state) {
                case "Interviewing voters" -> pollsterLabel.setBackground(Color.BLUE);
                case "Idle" -> pollsterLabel.setBackground(Color.GRAY);
                default -> pollsterLabel.setBackground(Color.WHITE);
            }
        });
    }

    /**
     * Update the state of the polling station in the GUI.
     * @param state The new state of the polling station.
     */
    public void updatePollingStationState(String state) {
        SwingUtilities.invokeLater(() -> {
            pollingStationLabel.setText("Polling Station: " + state);
            // Set background color based on state
            switch (state) {
                case "Open" -> pollingStationLabel.setBackground(Color.GREEN);
                case "Closed" -> pollingStationLabel.setBackground(Color.RED);
                default -> pollingStationLabel.setBackground(Color.WHITE);
            }
        });
    }

    /**
     * Update the number of voters inside the polling station in the GUI.
     * @param count The new number of voters inside the polling station.
     */
    public void updateVotersInside(int count) {
        SwingUtilities.invokeLater(() -> {
            votersInsideLabel.setText("Voters inside: " + count);
        });
    }

    /**
     * Update the number of voters processed in the GUI.
     * @param count The new number of voters processed.
     */
    public void updateVotersProcessed(int count) {
        SwingUtilities.invokeLater(() -> {
            votersProcessedLabel.setText("Voters processed: " + count);
        });
    }

    /**
     * Update the number of voters remaining in the GUI.
     * @param count The new number of voters remaining.
     */
    public void updateVotersRemaining(int count) {
        SwingUtilities.invokeLater(() -> {
            votersRemainingLabel.setText("Voters remaining: " + count);
        });
    }

    /**
     * Get the log area for displaying simulation events.
     * @return The JTextArea used for logging.
     */
    public JTextArea getLogArea() {
        return logArea;
    }

    /**
     * Stop the simulation.
     * This method is called when the "Stop Simulation" button is clicked.
     */
    private void stopSimulation() {
        PollingStation pollingStation = Main.getPollingStation();
        if (pollingStation != null) {
            pollingStation.close();
        }
    
        MyArrayList<Thread> threads = Main.getThreads();
        for (int i = 0; i < threads.size(); i++) {
            Thread thread = threads.get(i);
            if (thread != null) {
                thread.interrupt(); 
            }
        }
    
        Logger logger = Logger.getInstance("log.txt");
        logger.log("Simulation stopped by user");
    
    }
}