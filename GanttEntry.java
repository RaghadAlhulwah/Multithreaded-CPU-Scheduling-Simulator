package CS227;

public class GanttEntry {

    private int processId;
    private int startTime;
    private int endTime;
    private int startBurst;
    private int stopBurst;

    public GanttEntry(int processId,
                      int startTime,
                      int endTime,
                      int startBurst,
                      int stopBurst) {

        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startBurst = startBurst;
        this.stopBurst = stopBurst;
    }

    public int getProcessId() {
        return processId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getStartBurst() {
        return startBurst;
    }

    public int getStopBurst() {
        return stopBurst;
    }
}