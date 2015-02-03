package net.shockverse.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Tagette, LegitModern
 */
public class VoteManager extends HashMap<String, HashMap<String, Integer>> {
    
    public VoteManager() {
    }
    
    public void vote(String item, String name) {
        vote(item, name, 1);
    }
    
    public void vote(String item, String name, int worth) {
        vote(item, name, worth, null);
    }
    
    public void vote(String item, String name, int worth, List<String> removeFrom) {
        // Remove from other vote lists.
        for(String key : (removeFrom != null) ? removeFrom : keySet()) {
            get(key).remove(name);
        }
        if(!containsKey(item))
            put(item, new HashMap<String, Integer>());
        HashMap<String, Integer> votes = get(item);
        votes.put(name, worth);
    }
    
    public void addList(String item) {
        if(!containsKey(item))
            put(item, new HashMap<String, Integer>());
    }
    
    public int totalVotes(String item) {
        int votes = 0;
        if(containsKey(item))
            for(String name : get(item).keySet()) {
                int worth = get(item).get(name);
                votes += worth;
            }
        return votes;
    }
    
    public String getVote(String name) {
        String vote = null;
        for (String key : keySet()) {
            if(get(key).containsKey(name)) {
                vote = key;
            }
        }
        return vote;
    }
    
    public boolean hasVotedFor(String item, String name) {
        return get(item) != null && get(item).containsKey(name);
    }
    
    public void removeVotes(String name) {
        for (String key : keySet()) {
            get(key).remove(name);
        }
    }
    
    public String getMostVoted() {
        return getMostVoted(null);
    }
    
    public String getMostVoted(List<String> exclude) {
        String mostVotes = null; // -1 Means no arena has been voted on.
        // Find the arena withe the most votes.
        for (String key : keySet()) {
            if(exclude == null || !exclude.contains(key)) {
                HashMap<String, Integer> votes = get(key);
                HashMap<String, Integer> mostVoted = get(mostVotes);
                if (votes != null && (mostVoted == null || totalVotes(key) > totalVotes(mostVotes))) {
                    mostVotes = key;
                }
            }
        }
        return mostVotes;
    }
}
