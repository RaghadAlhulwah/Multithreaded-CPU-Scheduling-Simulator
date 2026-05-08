package operatingsystemproject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.ArrayList;

public class Sharedresources {

    /** Total available main memory (MB) - fixed by project spec */
    public static final int TOTAL_MEMORY = 2048;

    /** Round Robin time quantum (ms) - fixed by project spec */
    public static final int TIME_QUANTUM = 5;

    /** Priority scheduling starvation check multiplier - project spec: N x 5 ms */
    public static final int STARVATION_MULTIPLIER = 5;

    /** Aging interval - project spec: increase priority every 4 ms */
    public static final int AGING_INTERVAL = 4;

    public static final BlockingQueue<ProcessControlBlock> jobQueue   = new LinkedBlockingQueue<>();

    public static final BlockingQueue<ProcessControlBlock> readyQueue = new LinkedBlockingQueue<>();

    public static int usedMemory = 0;

    public static final Object memoryLock = new Object();

    public static volatile boolean inputFinished = false;

    public static volatile boolean allProcessesDone = false;

    public static final List<Integer> starvedProcesses = new ArrayList<>();

    public static int availableMemory() {
        return TOTAL_MEMORY - usedMemory;
    }

    private Sharedresources() {}
}
