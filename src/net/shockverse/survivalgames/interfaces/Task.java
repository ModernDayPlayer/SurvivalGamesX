package net.shockverse.survivalgames.interfaces;

public interface Task {
    
    public void run();
    public void onFinish();
    public boolean hasFinished();
    public boolean hasRun();
    public int getTimesRun();
    
}
