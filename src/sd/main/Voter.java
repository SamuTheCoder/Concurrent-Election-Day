import java.util.Random;

/**
 * Voter is a class that represents a voter that is going to vote on the Election Day
 * @class Voter
 */
public class Voter implements Runnable{

    private int id;
    private VotingParties party;
    private static final Random random = new Random();
    private PollingStation pollingStation;
    private boolean isApproved;
    private boolean hasVoted;
    private boolean isRejectedByPollClerk;
    private ExitPoll exitPoll;
    private double respnseProbability; 
    private double lieProbability;
    private boolean wasInterviewed;
    private boolean isElectionDayDone;
    private Logger logger;
    
    public Voter(){}

    //Constructor
    public Voter(int id, PollingStation pollingStation, ExitPoll exitPoll, double responseProbability, double lieProbability){
        this.id = id;
        this.party = chooseParty();
        this.pollingStation = pollingStation;   
        this.isApproved = false; 
        this.hasVoted = false;
        this.isRejectedByPollClerk = false;
        this.exitPoll = exitPoll;
        this.respnseProbability = responseProbability;
        this.lieProbability = lieProbability;
        this.wasInterviewed = false;
        this.isElectionDayDone = false;
        this.logger = Logger.getInstance("log.txt");
    }

    //run method
    public void run(){
        ElectionSimulationGUI gui = Main.getGUI();
        gui.updateVoterState(id, "Waiting to enter");

        //System.out.printf("* Created Voter with ID: %s\n", id);
        logger.log("* Created Voter with ID: "+ id);
        do{
            if(this.isElectionDayDone){
                //System.out.printf("* Voter %d - day done, breaking", this.id);
                logger.log("* Voter "+ this.id +" - day done, breaking");
                gui.updateVoterState(id, "Terminated");
                break;
            }
            // Voters wait for polling station to open
            synchronized(pollingStation){
                while(!pollingStation.pollingStationIsOpen()){
                    try{
                        pollingStation.wait();
                    } catch (InterruptedException e){
                        logger.log("Voter "+ this.id +" interrupted while waiting for polling station to open");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        
            pollingStation.getDesk().enterDeskQueue(this, pollingStation);
            gui.updateVoterState(id, "Entering polling station");
            if(!pollingStation.pollingStationIsOpen()){
                //System.out.println("* Election Day is over, Voter %d leaving Desk " + this.getVoterId());
                logger.log("* Election Day is over, Voter "+ this.getVoterId() +" leaving Desk ");
                pollingStation.decrementCurrentVoters();
                gui.updateVoterState(id, "Left pollin station");
                break;
            }

            // ID check by poll clerk
            synchronized(this){
                while(!isApproved){
                    try{
                        this.wait();
                        if(this.isRejectedByPollClerk){ //for voters that entered but can't vote because the limit was reached
                            break;
                        }
                    } catch (InterruptedException e){
                        logger.log("Voter "+ this.id +" interrupted while waiting for approval");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            if(this.isRejectedByPollClerk){
                if(!pollingStation.pollingStationIsOpen())
                {
                    //System.out.println("* Poll station closed, Voter " + this.getVoterId() + " exits the polling station");
                    logger.log("* Poll station closed, Voter " + this.getVoterId() + " exits the polling station");
                    pollingStation.decrementCurrentVoters();
                    synchronized(pollingStation.getDesk().getPollClerk()){
                        pollingStation.getDesk().getPollClerk().notifyAll();
                    }
                    gui.updateVoterState(id, "Rejected by poll clerk and left polling station");
                    break;
                }
                pollingStation.decrementCurrentVoters();
                this.rebornVoter();
                gui.updateVoterState(id, "Voter Reborn");
                continue;
            }

            //Approved -> go to voting booth
            pollingStation.getVotingBooth().enterBoothQueue(this);
            gui.updateVoterState(id, "Approved and voting");
            pollingStation.getVotingBooth().castVote(this); 

            synchronized(this){
                while(!hasVoted){
                    try{
                        this.wait();
                    } catch (InterruptedException e){
                        logger.log("Voter "+ this.id +" interrupted while waiting to vote");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            pollingStation.getVotingBooth().exitBoothQueue();
            pollingStation.decrementCurrentVoters();
            gui.updateVoterState(id, "Voted and exiting");
            synchronized(pollingStation){ //notify voters that are waiting for space to enter the polling station (enterDeskQueue)
                pollingStation.notifyAll();
            }

            // Add voter to exit poll
            synchronized(exitPoll){
                exitPoll.addVoter(this);
            }

            synchronized(this){
                try {
                    while(!this.wasInterviewed){
                        wait(); //wait for pollster to signal that the potential interview is done
                    }
                } catch (InterruptedException e){
                    logger.log("Voter "+ this.id +" interrupted while waiting for pollster");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            PollClerk tmp = pollingStation.getDesk().getPollClerk();
            synchronized(tmp){ //notify the PollClerk for him to check if all voters have voted and the pollingStation is empty and ready for results
                tmp.notify();
            } 


            this.rebornVoter();
            gui.updateVoterState(id, "Reborn and re-entering");
        }while(pollingStation.pollingStationIsOpen() || !Thread.interrupted());
        
        //System.out.printf("* Voter %d terminated\n", this.id);
        logger.log("* Voter "+ this.id +" terminated");
        gui.updateVoterState(id, "Terminated");
    }

    /**
     * Getter for voter ID
     * @return voter's id
     */
    public int getVoterId(){
        return id;
    }

    /**
     * Getter for voter's party
     * @return party
     */
    public VotingParties getParty(){
        return party;
    }

    /**
     * Select a random party for the voter to answer to the pollster
     * @param actualParty
     * @return party
     */
    private VotingParties getRandomParty(VotingParties actualParty){
        VotingParties[] parties = VotingParties.values();
        VotingParties randomParty;
        do{
            randomParty = parties[random.nextInt(parties.length)];
        } while(randomParty == actualParty);
        return randomParty;
        
    }

    /**
     * Getter for wasInterviewd
     * @return wasInterviewed
     */
    private boolean getWasInterviewd(){
        return this.wasInterviewed;
    }

    /** 
     * Setter for wasInterviewed
     */
    public void setWasInterviewd(){
        this.wasInterviewed = !this.wasInterviewed;
    }

    /** 
     * Setter for isApproved
     */
    public void setApproved(boolean isApproved){
        this.isApproved = isApproved;
    }

    public String toString(){
        return "Voter ID: " + id + " Party: " + party;
    }

    /**
     * Vot
     * @return
     */
    private static VotingParties chooseParty() {
        // define the weights for each party
        int[] weights = {50, 20, 20, 10}; // PPS will have the highest weight
        int totalWeight = 0;

        for (int weight : weights) {
            totalWeight += weight;
        }

        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return VotingParties.values()[i];
            }
        }

        // default return (should never reach here if weights are set correctly)
        return VotingParties.PPS;
    }

    /**
     * Getter for hasVoted
     * @return hasVoted
     */
    public boolean hasVoted(){
        return hasVoted;
    }

    /**
     * Setter for hasvoted
     * @param hasVoted
     */
    public void setHasVoted(boolean hasVoted){
        this.hasVoted = hasVoted;
    }

    /**
     * Setter for isRejectedByPollClerk, used for handling rejected voters
     */
    public void setIsRejectedByPollClerk(){
        this.isRejectedByPollClerk = true;
    }

    /**
     * Handles answering to the Pollster (or not)
     */
    public void respondToPollster(){
        try {
            Thread.sleep((int)(Math.random() * (10 - 5) + 5)* Main.getSpeedMultiplier()); 
        } catch (InterruptedException e) {
            logger.log("Voter " + this.id + " interrupted while responding to pollster");
            Thread.currentThread().interrupt();
            return;
        }

        if(random.nextDouble() < respnseProbability){       //responds
            if(random.nextDouble() < lieProbability){       //lies
                VotingParties liedParty = getRandomParty(this.party);
                //System.out.println("* Voter " + this.getVoterId() + " lied to the pollster about his party. He said he voted in " + liedParty);
                logger.log("* Voter " + this.getVoterId() + " lied to the pollster about his party. He said he voted in " + liedParty);
            }else{ // tells the truth
                //System.out.println("* Voter " + this.getVoterId() + " told the pollster he voted in " + this.party);
                logger.log("* Voter " + this.getVoterId() + " told the pollster he voted in " + this.party);
            }
        }else{ //doesn't respond
            //System.out.println("* Voter " + this.getVoterId() + " didn't respond to the pollster");
            logger.log("* Voter " + this.getVoterId() + " didn't respond to the pollster");
        }
    }

    /**
     * Reborns a voter thread with new parameters
     */
    public void rebornVoter(){
        double rebornProbability = 0.95;

        int prevId = this.id;
        this.hasVoted = false;
        this.isApproved = false;
        this.isRejectedByPollClerk = false;
        this.party = chooseParty();
        this.setWasInterviewd();
        if(Math.random() <= rebornProbability){
            this.id = pollingStation.getVoterIdOffset();
            //System.out.printf("* Voter %d reborn into %d\n", prevId, this.id);
            logger.log("* Voter "+ prevId +" reborn into "+ this.id);
            ElectionSimulationGUI gui = Main.getGUI();
            gui.updateVoterState(prevId, "Terminated");
            gui.updateVoterState(this.id, "Reborn and re-entering");

        }
        else
            //System.out.printf("* Voter %d reborn false \n", this.id);
            logger.log("* Voter "+ this.id +" reborn false");
    }
}