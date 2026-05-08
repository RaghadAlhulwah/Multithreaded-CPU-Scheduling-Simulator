package operatingsystemproject;

public class ProcessControlBlock {

    // -------------------------------------------------------------------------
    // Process States
    // -------------------------------------------------------------------------
    public enum State {
        NEW,        // Just created, waiting in job queue
        READY,      // In ready queue, waiting for CPU
        RUNNING,    // Currently executing on CPU
        TERMINATED  // Finished execution
    }

    // -------------------------------------------------------------------------
    // Core PCB Fields  (required by project specification)
    // -------------------------------------------------------------------------
    private int    processId;        // Unique process identifier  (e.g. 1, 2, 3 ...)
    private State  state;            // Current state of the process
    private int    burstTime;        // Total CPU burst time (ms)
    private int    priority;         // Priority number 1-30  (1 = highest)
    private int    memoryRequired;   // Memory required (MB)
    private int    waitingTime;      // Total time spent waiting in ready queue (ms)
    private int    turnaroundTime;   // Total time from arrival to completion (ms)

    // -------------------------------------------------------------------------
    // Extra Helper Fields  (added to support scheduling logic)
    // -------------------------------------------------------------------------
    private int    arrivalTime;      // Time process entered job queue  (used for tie-breaking)
    private int    startTime;        // First time process got the CPU  (-1 = not started yet)
    private int    terminationTime;  // Time process finished execution (-1 = not finished)
    private int    remainingBurst;   // Remaining CPU time (changes during Round Robin)
    private int    timeInQueue;      // Tracks consecutive ms spent in ready queue (for starvation)
    private int    originalPriority; // Stores the initial priority before any aging is applied

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a PCB with data parsed from job.txt.
     *
     * @param processId      Unique process ID
     * @param burstTime      CPU burst time in ms
     * @param priority       Priority number (1-30)
     * @param memoryRequired Memory required in MB
     * @param arrivalTime    Simulated arrival time (order in file, all start at time 0)
     */
    public ProcessControlBlock(int processId, int burstTime, int priority, int memoryRequired, int arrivalTime) {
        this.processId       = processId;
        this.burstTime       = burstTime;
        this.priority        = priority;
        this.originalPriority = priority;
        this.memoryRequired  = memoryRequired;
        this.arrivalTime     = arrivalTime;

        // Initial state: process just created, not yet in ready queue
        this.state           = State.NEW;

        // Scheduling metrics - set to 0 or -1 (unknown) until simulation runs
        this.waitingTime      = 0;
        this.turnaroundTime   = 0;
        this.startTime        = -1;
        this.terminationTime  = -1;

        // Remaining burst equals full burst at creation
        this.remainingBurst  = burstTime;

        // Starvation counter
        this.timeInQueue     = 0;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int   getProcessId()       { return processId; }
    public State getState()           { return state; }
    public int   getBurstTime()       { return burstTime; }
    public int   getPriority()        { return priority; }
    public int   getOriginalPriority(){ return originalPriority; }
    public int   getMemoryRequired()  { return memoryRequired; }
    public int   getWaitingTime()     { return waitingTime; }
    public int   getTurnaroundTime()  { return turnaroundTime; }
    public int   getArrivalTime()     { return arrivalTime; }
    public int   getStartTime()       { return startTime; }
    public int   getTerminationTime() { return terminationTime; }
    public int   getRemainingBurst()  { return remainingBurst; }
    public int   getTimeInQueue()     { return timeInQueue; }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setState(State state)                  { this.state = state; }
    public void setPriority(int priority)              { this.priority = priority; }
    public void setWaitingTime(int waitingTime)        { this.waitingTime = waitingTime; }
    public void setTurnaroundTime(int turnaroundTime)  { this.turnaroundTime = turnaroundTime; }
    public void setStartTime(int startTime)            { this.startTime = startTime; }
    public void setTerminationTime(int terminationTime){ this.terminationTime = terminationTime; }
    public void setRemainingBurst(int remainingBurst)  { this.remainingBurst = remainingBurst; }
    public void setTimeInQueue(int timeInQueue)        { this.timeInQueue = timeInQueue; }

    // -------------------------------------------------------------------------
    // Convenience Increment Methods  (used by scheduler each ms tick)
    // -------------------------------------------------------------------------

    /** Increments waiting time by 1 ms. Called each tick a process is in the ready queue. */
    public void incrementWaitingTime()  { this.waitingTime++; }

    /** Increments starvation counter by 1 ms. */
    public void incrementTimeInQueue()  { this.timeInQueue++; }

    /** Resets starvation counter after process gets CPU or after aging is applied. */
    public void resetTimeInQueue()      { this.timeInQueue = 0; }

    /** Decrements remaining burst by 1 ms. Called each ms the process runs on CPU. */
    public void decrementRemainingBurst() {
        if (this.remainingBurst > 0) this.remainingBurst--;
    }

    /**
     * Applies one aging step: decreases priority number by 1 (raises priority).
     * Priority cannot go below 1 (the highest possible priority).
     */
    public void applyAging() {
        if (this.priority > 1) {
            this.priority--;
        }
    }

    /** Returns true if this process has finished all its CPU work. */
    public boolean isFinished() {
        return this.remainingBurst == 0;
    }

    // -------------------------------------------------------------------------
    // toString - useful for debugging and output
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format(
            "PCB[ID=%-3d | State=%-10s | Burst=%-3d | Remaining=%-3d | " +
            "Priority=%-2d | Memory=%-4dMB | Arrival=%-3d | " +
            "Start=%-4s | End=%-4s | Wait=%-3d | TAT=%-3d]",
            processId,
            state,
            burstTime,
            remainingBurst,
            priority,
            memoryRequired,
            arrivalTime,
            (startTime == -1       ? "N/A" : String.valueOf(startTime)),
            (terminationTime == -1 ? "N/A" : String.valueOf(terminationTime)),
            waitingTime,
            turnaroundTime
        );
    }
}
