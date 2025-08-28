// Polling Station will implement monitors for synchronization between voters
public class PollingStation{
    private final int stationCapacity; // Maximum voters that can be in the polling station at once
    private final int voterLimit; // Maximum number of voters to process
    private int voterOffset; // Number of voters processed
    private int currentVoters; // Number of voters currently in the polling station
    private boolean isOpen;
    private Desk desk;
    private VotingBooth votingBooth;
    private int voterIdOffset; // Id offset for reborning voters
    private Logger logger;


    public PollingStation(int capacity, int voterLimit, PollClerk pollClerk, int voterIdOffset){
        this.voterLimit = voterLimit;
        this.stationCapacity = capacity;
        this.isOpen = false; // polling station starts closed
        this.votingBooth = new VotingBooth();
        this.desk = new Desk(pollClerk);
        this.voterOffset = 0;
        this.currentVoters = 0;
        this.voterIdOffset = voterIdOffset;
        this.logger = Logger.getInstance("log.txt");
    }

    public synchronized void open(){
        this.isOpen = true;
        //System.out.println("The polling station is open.");
        logger.log("The polling station is open.");
        ElectionSimulationGUI gui = Main.getGUI();
        gui.updatePollingStationState("Open");
        notifyAll(); // Notify all voters that the polling station is open (voters are waiting on the station's monitor)
    }

    public synchronized void close(){
        this.isOpen = false;
        //System.out.println("The polling station is closed.");
        logger.log("The polling station is closed.");
        ElectionSimulationGUI gui = Main.getGUI();
        gui.updatePollingStationState("Closed");
        notifyAll(); // Notify all voters that the polling station is closed
    }

    public synchronized boolean pollingStationIsOpen(){
        return this.isOpen;
    }

    public int getStationCapacity(){
        return this.stationCapacity;
    }    

    public Desk getDesk(){
        return this.desk;
    }

    public int getVoterLimit(){
        return this.voterLimit;
    }

    public synchronized int getVoterOffset(){
        return this.voterOffset;
    }

    public synchronized int getCurrentVoters(){
        return this.currentVoters;
    }

    public VotingBooth getVotingBooth(){
        return votingBooth;
    }

    public synchronized int getVoterIdOffset(){
        voterIdOffset++;
        return voterIdOffset;
    }

    public synchronized void incrementVoterOffset(){
        if(this.voterOffset == this.voterLimit){
            //System.out.println("Maximum number of voters reached.");
            logger.log("Maximum number of voters reached.");
            return;
        }
        this.voterOffset++;
    }


    public synchronized void incrementCurrentVoters(){
        this.currentVoters++;
        ElectionSimulationGUI gui = Main.getGUI();
        gui.updateVotersInside(this.currentVoters);
    }

    public synchronized void decrementCurrentVoters(){
        this.currentVoters--;
        ElectionSimulationGUI gui = Main.getGUI();
        gui.updateVotersInside(this.currentVoters);
    }
}