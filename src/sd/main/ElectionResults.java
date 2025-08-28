/**
 * ElectionResults.java
 * This class represents the results of the election.
 * It stores and manages the vote counts for each political party.
 * The class provides methods to increment, clear, and display the vote counts,
 * as well as process a list of votes.
 */
class ElectionResults {
    private final int[] voteCounts; // Array to store vote counts for each party
    private final VotingParties[] keys; // Array to store the keys (parties)
    private Logger logger;

    // Constructor
    public ElectionResults() {
        keys = VotingParties.values();
        voteCounts = new int[keys.length];
        this.logger = Logger.getInstance("log.txt");
    }

     /**
     * Put a vote count for a party.
     * @param key The party to update.
     * @param value The vote count to set.
     */
    public void put(VotingParties key, int value) {
        int index = key.ordinal(); 
        voteCounts[index] = value; 
    }

    /**
     * Get the vote count for a party.
     * @param key The party to query.
     * @return The vote count for the specified party.
     */
    public int get(VotingParties key) {
        int index = key.ordinal();
        return voteCounts[index]; 
    }

    /**
     * Increment the vote count for a party.
     * @param key The party to increment.
     */
    public void increment(VotingParties key) {
        int index = key.ordinal(); 
        voteCounts[index]++; 
    }

    /**
     * Clear the vote count for a party.
     * @param key The party to clear.
     */
    public void clear(VotingParties key) {
        int index = key.ordinal(); 
        voteCounts[index] = 0; 
    }

    /**
     * Process a list of votes and update the vote counts.
     * @param votes The list of votes to process.
     */
    public void processVotes(MyArrayList<VotingParties> votes) {
        for (int i = 0; i < votes.size(); i++) {
            VotingParties vote = votes.get(i); 
            increment(vote);
        }
    }

    /**
     * Display the vote counts for all parties.
     */
    public void display() {
        for (int i = 0; i < keys.length; i++) {
            logger.log(keys[i] + " : " + voteCounts[i]);
        }
    }

}