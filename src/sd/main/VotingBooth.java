/**
 * VotingBooth.java
 * This class represents the voting booth in the election simulation.
 * It manages a queue of voters who are waiting to cast their votes.
 * The voting booth ensures that voters cast their votes in a thread-safe manner.
 */
public class VotingBooth {
    private final MyArrayList<VotingParties> votes;
    private final GenericFIFO<Voter> boothQueue;
    private Logger logger;

    /**
     * Constructor for VotingBooth.
     */
    public VotingBooth() {
        this.votes = new MyArrayList<>();
        this.boothQueue = new GenericFIFO<>();
        this.logger = Logger.getInstance("log.txt");
    }

    /**
     * Cast a vote for a voter.
     * This method ensures that voters cast their votes in the order they entered the queue.
     * @param voter The voter casting the vote.
     */
    public synchronized void castVote(Voter voter) {

        while(boothQueue.front() != voter){
            try {
                wait(); //wait for turn

            } catch (InterruptedException e) {
                logger.log("Voter " + voter.getVoterId() + " was interrupted while waiting for turn to vote");
                Thread.currentThread().interrupt();
                return;
            }

        }

        // time to vote (0-15 ms)
        try {
            Thread.sleep((int)(Math.random() * 15) * Main.getSpeedMultiplier());
        } catch (InterruptedException e) {
            logger.log("Voter " + voter.getVoterId() + " was interrupted while voting");
            Thread.currentThread().interrupt();
            return;
        }

        // record vote
        votes.add(voter.getParty());
        //System.out.println("Voter " + voter.getVoterId() + " voted anonymously");
        logger.log("Voter " + voter.getVoterId() + " voted anonymously");
        
        voter.setHasVoted(true);
        
        synchronized(voter){
            voter.notify();
        }
        //pollingStation.voterFinished();, looks like garbage

    }
    
    /**
     * Add a voter to the voting booth queue.
     * @param voter The voter to be added to the queue.
     */
    public synchronized void enterBoothQueue(Voter voter){
        boothQueue.enQueue(voter);
        notifyAll();
    }

    /**
     * Remove a voter from the voting booth queue.
     * @return The voter removed from the queue.
     */
    public synchronized Voter exitBoothQueue(){   
        Voter v = boothQueue.deQueue();
        notifyAll();
        return v;
    }

    /**
     * Get the list of votes cast in the voting booth.
     * @return The list of votes.
     */
    public synchronized MyArrayList<VotingParties> getVotes() {
        return votes;
    }
}
