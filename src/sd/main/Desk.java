/**
 * Desk.java
 * This class represents a desk in the polling station.
 * It has a queue of voters waiting to get approval to vote (that just arrived).
 * The desk is assigned to a poll clerk, which uses it to manage entering voters
 */
public class Desk {
    private GenericFIFO<Voter> deskQueue;
    private PollClerk pollClerk;
    private Logger logger;

    /**
     * Constructor for Desk
     */
    public Desk(PollClerk pollClerk){
        this.pollClerk = pollClerk;
        this.deskQueue = new GenericFIFO<Voter>();
        this.logger = Logger.getInstance("log.txt");
    }

    /**
     * Add a voter to the Desk queue
     * @param v Voter to be added to the queue
     */
    public void enterDeskQueue(Voter v, PollingStation pollingStation){
        synchronized(pollingStation){
            while(pollingStation.getStationCapacity() <= pollingStation.getCurrentVoters() && pollingStation.pollingStationIsOpen()){ //wait for space inside polling station
                try{
                    //System.out.println("Voter " + v.getVoterId() + " Waiting for space to enter at polling Station");
                    logger.log("Voter " + v.getVoterId() + " Waiting for space to enter at polling Station");
                    pollingStation.wait();
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }    
        synchronized(v){
            if(!pollingStation.pollingStationIsOpen()){//when the station closes, voters will not enter the desk queue and will leave
                //System.out.println("-------------- REJECTED");
                logger.log("Voter " + v.getVoterId() + " can't enter Desk, Election Day Over");
                v.notify();
                return;
            }
        }
        pollingStation.incrementCurrentVoters();
        synchronized( this){
            //System.out.println("- Voter " + v.getVoterId() + " entering queue, " + pollingStation.getStationCapacity());
            logger.log("- Voter " + v.getVoterId() + " entering queue, " + pollingStation.getStationCapacity());
            deskQueue.enQueue(v);
            notifyAll(); //notify that Desk is not empty
        }
    }

    /**
     * Remove a voter from the Desk queue
     * @return Removed voter
     */
    public synchronized Voter exitDeskQueue(){
        while(deskQueue.isEmpty()){
            try{
                wait();
            } catch (InterruptedException e){
                logger.log("Desk was interrupted while waiting for voters.");
                Thread.currentThread().interrupt();
                return null;
            }
        }
        
        Voter v = deskQueue.deQueue();
        if(v != null){
            logger.log("Voter " + v.getVoterId() + " exiting desk queue");
        }
        notifyAll(); //notify that desk its not full
        return v;
    }

    /**
     * Get the PollClerk assigned to the Desk
     * @return PollClerk
     */
    public PollClerk getPollClerk(){
        return pollClerk;
    }

    /**
     * Checks if Desk Queue is empty
     * @return true if empty, else false
     */
    public boolean isEmpty(){
        return deskQueue.isEmpty();
    }
}
