package net.shockverse.survivalgames.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.shockverse.survivalgames.interfaces.Task;
import org.bukkit.event.Cancellable;

/**
 *
 * @author Tagette
 */
public class TaskManager {

    public HashMap<String, List<Task>> tasks;
    
    public TaskManager() {
        tasks = new HashMap<String, List<Task>>();
    }
    
    public List<Task> getTasks() {
        List<Task> allTasks = new ArrayList<Task>();
        for(String tag : tasks.keySet()) {
            allTasks.addAll(tasks.get(tag));
        }
        return allTasks;
    }
    
    public List<Task> getTasks(String tag) {
        return tasks.get(tag);
    }
    
    public void addTask(Task task) {
        addTask(null, task);
    }
    
    public void addTask(String tag, Task task) {
        if(!tasks.containsKey(tag))
            tasks.put(tag, new ArrayList<Task>());
        tasks.get(tag).add(task);
    }
    
    public void clearTasks() {
        for(String tag : tasks.keySet())
            clearTasks(tag);
        tasks.clear();
    }
    
    public void clearTasks(String tag) {
        if(tasks.containsKey(tag)) {
            for(Task task : tasks.get(tag)) {
                if(task instanceof Cancellable) {
                    ((Cancellable) task).setCancelled(true);
                }
            }
            tasks.get(tag).clear();
        }
    }
    
}
