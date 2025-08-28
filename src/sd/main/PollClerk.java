
/**
 * PollClerk is the class that represents the entity who is going to check
 * Voters (from Desk at PollingStation) so that they can be approved to vote (or not)
 * @class PollClerk
 */
public class PollClerk implements Runnable{

    private MyArrayList<Integer> idList;
    private PollingStation pollingStation;
    private int clerkID;
    private int processedVoters;
    private ElectionResults electionResults;
    private boolean hasVotersToInterview;
    private Logger logger;

    public PollClerk(){}

    //Constructor
    public PollClerk(PollingStation pollingStation, int clerkID, ElectionResults electionResults){
        this.clerkID = clerkID;
        this.idList = new MyArrayList<Integer>();
        this.pollingStation = pollingStation;
        this.processedVoters = 0;
        this.electionResults = electionResults;
        this.hasVotersToInterview = true;
        this.logger = Logger.getInstance("log.txt");
    }

    //run method
    public void run(){
        ElectionSimulationGUI gui = Main.getGUI();        
        try{
            
            pollingStation.open();
            //System.out.println("- Hello, I am Poll Clerk " + clerkID);
            logger.log("- Hello, I am Poll Clerk " + clerkID);
            gui.updatePollClerkState("Processing voters");

            while(!Thread.interrupted()){
                if(pollingStation.getVoterOffset() == pollingStation.getVoterLimit()){ //limit reached, reject remaining voters on desk queue
                    //System.out.println("Reached the mark of voters");
                    while(!pollingStation.getDesk().isEmpty()){
                        Voter v = pollingStation.getDesk().exitDeskQueue();
                        if(v != null){
                            checkVoterID(v); //voters will be rejected
                        }
                    }
                    break;
                }
                else{ //normal check of voters
                    Voter v = pollingStation.getDesk().exitDeskQueue();
                    if(v != null){
                        checkVoterID(v);
                    }
                }
            }

            synchronized (this){ //Wait until all voters waiting on booth have voted (after voter limit reached and station closed)
                while(pollingStation.getCurrentVoters() > 0){
                    try{
                        //System.out.println("- Current voters at Poll Clerk: " + pollingStation.getCurrentVoters());
                        logger.log("- Current voters at Poll Clerk: " + pollingStation.getCurrentVoters());
                        wait();
                    } catch (InterruptedException e){
                        logger.log("- Poll Clerk " + clerkID + " was interrupted while waiting for voters.");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            this.hasVotersToInterview = false;
            endElection();
            gui.updatePollClerkState("Election ended");



        } catch (Exception e){
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    /**
     * Checks wether a voter is valid to vote or not
     * @param v (voter) to check
     */
    private void checkVoterID(Voter v){
        //System.out.printf("- Poll Clerk %d is processing Voter %s\n", clerkID, v.getVoterId());
        logger.log("- Poll Clerk " + clerkID + " is processing Voter " + v.getVoterId());
        try{
            Thread.sleep((int)(Math.random() * (10 - 5) + 5)* Main.getSpeedMultiplier()); // 5-10ms random id verification time *  Speed Multiplier
        } catch (Exception e){
            logger.log("- Poll Clerk " + clerkID + " was interrupted while processing Voter " + v.getVoterId());
            Thread.currentThread().interrupt();
            return;
        }

        try{
            if(v != null){
                if(isValidVoter(v)){
                    if(pollingStation.getVoterOffset() >= pollingStation.getVoterLimit()){ //reject voters that entered after the limit of voters reached
                        v.setIsRejectedByPollClerk();
                        synchronized(v){
                            v.notify();
                        }
                        return;
                    }
                    pollingStation.incrementVoterOffset();
                    ElectionSimulationGUI gui = Main.getGUI();
                    gui.updateVotersProcessed(pollingStation.getVoterOffset());
                    gui.updateVotersRemaining(pollingStation.getVoterLimit() - pollingStation.getVoterOffset());
                    if(pollingStation.getVoterOffset() >= pollingStation.getVoterLimit()){ //close the polling station in case of limit reached
                        //System.out.printf("- Voter Limit reached, Poll Clerk " + this.clerkID + " is closing the Polling Station\n");
                        logger.log("- Voter Limit reached, Poll Clerk " + this.clerkID + " is closing the Polling Station");
                        pollingStation.close();
                    }
                    idList.add(v.getVoterId());
                    v.setApproved(true);
                    //System.out.printf("- Voter %s has been approved by Poll Clerk %d\n", v.getVoterId(), clerkID);
                    logger.log("- Voter " + v.getVoterId()  + " has been approved by Poll Clerk " + clerkID);
                    synchronized(v){
                        v.notify();
                    }
                }
                else{
                    v.setIsRejectedByPollClerk();
                    //System.out.printf("- Voter %s has been rejected by Poll Clerk %d\n", v.getVoterId(), clerkID);
                    logger.log("- Voter " + v.getVoterId() + " has been rejected by Poll Clerk " + clerkID);
                    synchronized(v){
                        v.notify();
                    }
                }
            }  
        } catch (Exception e){
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    /**
     * End an election, count votes and display them
     */
    private void endElection(){
        //get booth votes
        MyArrayList<VotingParties> votes = pollingStation.getVotingBooth().getVotes();

        //count votes
        electionResults.processVotes(votes);

        //System.out.println("Election Results:");
        logger.log("Election Results:");
        electionResults.display();

    }

    /**
     * Assigns a PollingStation to the Poll Clerk
     * @param pollingStation
     */
    public void setPollingStationToClerk(PollingStation pollingStation){
        this.pollingStation = pollingStation;
    }

    /**
     * Check if a voter is valid or not
     * @param v
     * @return true if valid, else false
     */
    public boolean isValidVoter(Voter v){
        return !idList.query(v.getVoterId());
    }

    /**
     * Getter for Clerk ID
     * @return clerkID
     */
    public int getClerkID(){
        return clerkID;
    }  

    /**
     * Check if there are voters to interview
     * @return hasVotersToInterview
     */
    public boolean getHasVotersToInterview(){
        return hasVotersToInterview;
    }
}