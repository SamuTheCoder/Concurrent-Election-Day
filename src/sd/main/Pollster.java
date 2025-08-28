/**
 * Pollster.java
 * This class represents the pollster in the election simulation.
 * The pollster is responsible for interviewing voters who have finished voting and are in the exit poll queue.
 * The pollster selects voters randomly based on a selection probability and records their responses.
 */
import java.util.Random;

/**
 * Pollster Class Represents the pollster who is going to choose voters
 * in the ExitPoll to interview
 * @class Pollster
 */
public class Pollster implements  Runnable{
    private final ExitPoll exitPoll;
    private final Random random;
    private final double selectionProbability;
    private final PollingStation pollingStation;
    private final PollClerk stationClerk;
    private int interviewsLimit; //Pollster keeps track of the limit of voters to interview
    private int interviewOffset;
    private Logger logger;

    /**
     * Constructor for Pollster.
     */
    public Pollster(ExitPoll exitPoll, double selectionProbability, PollingStation  pollingStation, PollClerk stationClerk, int interviewsLimit){
        this.exitPoll = exitPoll;
        this.random = new Random();
        this.selectionProbability = selectionProbability;
        this.pollingStation = pollingStation;
        this.stationClerk = stationClerk;
        this.interviewsLimit = interviewsLimit;
        this.interviewOffset = 0;
        this.logger = Logger.getInstance("log.txt");
    }

    
    /**
     * Run method for the pollster thread.
     * The pollster continuously interviews voters from the exit poll queue until the interview limit is reached.
     */
    @Override
    public void run() {
        ElectionSimulationGUI gui = Main.getGUI();
        gui.updatePollsterState("Interviewing voters");

        while (true) { 
            //System.out.printf("", stationClerk.getHasVotersToInterview());
            if(interviewOffset >= interviewsLimit){
                //System.out.println("> Pollster is done");
                logger.log("> Pollster is done");
                gui.updatePollsterState("Done");
                break;
            }
            Voter voter = exitPoll.removeVoter(); //get voter from exit poll

            if(voter == null){ //exit poll is empty
                continue;
            }
            
            if(random.nextDouble() < selectionProbability){ //pollster selects voter
                voter.respondToPollster();
            } else {
                //System.out.println("> Voter " + voter.getVoterId() + " was not selected by the pollster.");
                logger.log("> Voter " + voter.getVoterId() + " was not selected by the pollster.");
            }

            synchronized(voter){
                voter.setWasInterviewd();
                voter.notify();
            }

            interviewOffset++;
        }
    }
}
