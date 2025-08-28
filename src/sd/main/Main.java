import java.util.Scanner;

public class Main {
    private static ElectionSimulationGUI gui;
    private static int speedMultiplier = 1; // increase to slow down by *n times
    private static MyArrayList<Thread> threads = new MyArrayList<>(); // Store all threads
    private static PollingStation pollingStation;

    public static void main(String[] args) {
        // Initialize the GUI
        gui = new ElectionSimulationGUI();
        Logger.setLogArea(gui.getLogArea());
        Scanner sc = new Scanner(System.in);
        Logger logger = Logger.getInstance("log.txt");
        // Initialize the polling station, desk, and poll clerk
        //int pollingStationCapacity = 2; // Maximum capacity of the polling station
        //int voterLimit = 4; // Maximum number of voters to process
        int numVoters = setNumberOfVoters(sc); // Number of voters to simulate
        ElectionResults electionResults = new ElectionResults();
        PollClerk pollClerk = new PollClerk(null, 1, electionResults);

        pollingStation = new PollingStation(setPollingStationCapactiy(sc), setVoterLimit(sc,numVoters), pollClerk, numVoters);

        System.out.println("Program is running...");

        pollClerk.setPollingStationToClerk(pollingStation);
        
        ExitPoll exitPoll = new ExitPoll();

        Pollster pollster = new Pollster(exitPoll, 0.8, pollingStation, pollClerk, pollingStation.getVoterLimit());
        

        // Create and start voter threads
        for (int i = 1; i <= numVoters; i++) {
            Voter voter = new Voter(i, pollingStation, exitPoll, 0.6, 0.2);
            Thread voterThread = new Thread(voter);
            threads.add(voterThread);
            voterThread.start();
        }

        // Create and start the poll clerk thread
        Thread pollClerkThread = new Thread(pollClerk);
        threads.add(pollClerkThread);
        pollClerkThread.start();
        
        // Create and start the pollster thread
        Thread pollsterThread = new Thread(pollster);
        threads.add(pollsterThread);
        pollsterThread.start();

        // Wait for all voters to be processed
        try {
            pollClerkThread.join(); // Wait for the poll clerk thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        try {
            pollsterThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        // Close the polling station
        //System.out.println("Polling station is closed. All voters processed.");
        logger.log("Polling station is closed. All voters processed.");

        System.out.println("Written all logs to \"log.txt\" file");

        logger.close();
        sc.close();
    }
    
    public static int setPollingStationCapactiy(Scanner sc){
        System.out.print("Polling station capacity: ");
        int pollingStationCapacity = Integer.parseInt(sc.nextLine());

        if(pollingStationCapacity < 2 || pollingStationCapacity > 5){
            System.err.println("Error: Polling Station capacity must be between 2 and 5.");
            System.exit(0);
        }

        return pollingStationCapacity;
    }

    public static int setNumberOfVoters(Scanner sc){
        System.out.print("Number of voters: ");
        int numOfVoters = Integer.parseInt(sc.nextLine());

        if(numOfVoters < 3 || numOfVoters > 10){
            System.err.println("Error: Number of voters must be between 3 and 10.");
            System.exit(0);
        }

        return numOfVoters;
    }

    public static int setVoterLimit(Scanner sc, int numOfVoters){
        System.out.print("Maximum number of voters to be processed: ");
        int numOfVotersToProcess = Integer.parseInt(sc.nextLine());

        return numOfVotersToProcess;
    }

    public static ElectionSimulationGUI getGUI(){
        return gui;
    }

    public static int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public static void setSpeedMultiplier(int multiplier) {
        speedMultiplier = multiplier;
    }

    public static MyArrayList<Thread> getThreads() {
        return threads;
    }

    public static PollingStation getPollingStation() {
        return pollingStation;
    }
}
