/**
 * ExitPoll.java
 * This class represents the exit poll in the election simulation.
 * It manages a queue of voters who have finished voting and are waiting to be interviewed by the pollster.
 * The exit poll ensures that voters are added and removed in a thread-safe manner.
 */
public class ExitPoll {
    private final GenericFIFO<Voter> exitPollQueue;
    private boolean isPollingStationOpen;

    /**
     * Constructor for ExitPoll.
     */
    public ExitPoll(){
        this.exitPollQueue = new GenericFIFO<>();
        this.isPollingStationOpen = true;
    }

    /**
     * Add a voter to the exit poll queue.
     * @param voter The voter to be added to the queue.
     */
    public synchronized void addVoter(Voter voter){
        if(isPollingStationOpen){
            exitPollQueue.enQueue(voter);
            notifyAll();
        }
    }

    /**
     * Remove a voter from the exit poll queue.
     * @return The voter removed from the queue, or null if the queue is empty.
     */
    public synchronized Voter removeVoter(){
        return exitPollQueue.isEmpty() ? null : exitPollQueue.deQueue();
    }

    /**
     * Close the exit poll.
     * This method is called when the polling station is closed.
     */
    public synchronized void close(){
        isPollingStationOpen = false;
        notifyAll();    // Notify the Pollster that polling is closed
    }

    /**
     * Check if the exit poll queue is empty.
     * @return True if the queue is empty, false otherwise.
     */
    public synchronized boolean isEmpty(){
        return exitPollQueue.isEmpty();
    }
}