/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operatingsystemproject;

import java.util.*;


public class CPUScheduler {

    // -- Constants from SharedResources (project spec) --
    private static final int TIME_QUANTUM         = Sharedresources.TIME_QUANTUM;         // 5 ms
    private static final int AGING_INTERVAL       = Sharedresources.AGING_INTERVAL;       // 4 ms
    private static final int STARVATION_MULT      = Sharedresources.STARVATION_MULTIPLIER; // 5

    // =========================================================================
    // MENU - Let user choose algorithm
    // =========================================================================

    public static void showMenuAndRun(ArrayList<ProcessControlBlock> readyQueue) {

        Scanner input = new Scanner(System.in);

        System.out.println("\n===== CPU Scheduling Simulator =====");
        System.out.println("1. Shortest Job First (SJF)");
        System.out.println("2. Round Robin (RR)");
        System.out.println("3. Priority Scheduling (Non-Preemptive)");
        System.out.print("Choose Algorithm (1-3): ");

        int choice = input.nextInt();

        // Work on a copy so the original readyQueue is not modified
        ArrayList<ProcessControlBlock> copy = copyProcesses(readyQueue);

        switch (choice) {
            case 1: runSJF(copy);         break;
            case 2: runRoundRobin(copy);   break;
            case 3: runPriority(copy);     break;
            default: System.out.println("Invalid choice. Please enter 1, 2, or 3.");
        }
    }

    // =========================================================================
    // ALGORITHM 1 - Shortest Job First (SJF)
    // =========================================================================

    public static void runSJF(ArrayList<ProcessControlBlock> processes) {

        System.out.println("\n===== SJF Scheduling =====");

        // Sort by burst time; tie-break by arrival order
        processes.sort((p1, p2) -> {
            if (p1.getBurstTime() != p2.getBurstTime()) {
                return p1.getBurstTime() - p2.getBurstTime();
            }
            return p1.getArrivalTime() - p2.getArrivalTime();
        });

        int currentTime = 0;
        ArrayList<GanttEntry> gantt = new ArrayList<>();

        for (ProcessControlBlock p : processes) {

            p.setStartTime(currentTime);
            p.setState(ProcessControlBlock.State.RUNNING);

            int startRemaining = p.getRemainingBurst();

            // Simulate 1 ms steps
            for (int i = 0; i < p.getBurstTime(); i++) {
                currentTime++;
                p.decrementRemainingBurst();
            }

            p.setTerminationTime(currentTime);
            p.setTurnaroundTime(p.getTerminationTime());                      // arrival = 0
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            p.setState(ProcessControlBlock.State.TERMINATED);

            gantt.add(new GanttEntry(
                    p.getProcessId(),
                    p.getStartTime(),
                    p.getTerminationTime(),
                    startRemaining,
                    0
            ));
        }

        OutputManager.printResults(processes, gantt, "SJF");
    }

    // =========================================================================
    // ALGORITHM 2 - Round Robin (RR), quantum = 5 ms
    // =========================================================================

    public static void runRoundRobin(ArrayList<ProcessControlBlock> processes) {

        System.out.println("\n===== Round Robin (q=" + TIME_QUANTUM + " ms) =====");

        Queue<ProcessControlBlock> queue = new LinkedList<>(processes);
        ArrayList<GanttEntry> gantt = new ArrayList<>();

        int currentTime = 0;

        while (!queue.isEmpty()) {

            ProcessControlBlock p = queue.poll();

            // Record first CPU assignment
            if (p.getStartTime() == -1) {
                p.setStartTime(currentTime);
            }

            p.setState(ProcessControlBlock.State.RUNNING);

            int start         = currentTime;
            int startRemaining = p.getRemainingBurst();
            int executeTime   = Math.min(TIME_QUANTUM, p.getRemainingBurst());

            // Simulate 1 ms steps
            for (int i = 0; i < executeTime; i++) {
                currentTime++;
                p.decrementRemainingBurst();
            }

            gantt.add(new GanttEntry(
                    p.getProcessId(),
                    start,
                    currentTime,
                    startRemaining,
                    p.getRemainingBurst()
            ));

            if (p.getRemainingBurst() > 0) {
                // Not done yet - go back to queue
                p.setState(ProcessControlBlock.State.READY);
                queue.add(p);

            } else {
                // Process finished
                p.setState(ProcessControlBlock.State.TERMINATED);
                p.setTerminationTime(currentTime);
                p.setTurnaroundTime(p.getTerminationTime());                  // arrival = 0
                p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            }
        }

        OutputManager.printResults(processes, gantt, "Round Robin");
    }

    // =========================================================================
    // ALGORITHM 3 - Priority Scheduling (Non-Preemptive) + Starvation + Aging
    // =========================================================================

    public static void runPriority(ArrayList<ProcessControlBlock> processes) {

        System.out.println("\n===== Priority Scheduling (Non-Preemptive) =====");

        ArrayList<ProcessControlBlock> ready     = new ArrayList<>(processes);
        ArrayList<ProcessControlBlock> completed = new ArrayList<>();
        ArrayList<GanttEntry>          gantt     = new ArrayList<>();

        // Use shared starvation list from Sharedresources
        Sharedresources.starvedProcesses.clear();

        int currentTime = 0;

        while (!ready.isEmpty()) {

          
            int n = ready.size();

            // Check starvation & apply aging using correct N
            applyStarvationAndAging(ready, currentTime, n);

            // Pick highest priority process (lowest number), tie-break by arrival
            ProcessControlBlock selected = getHighestPriorityProcess(ready);
            ready.remove(selected);

            if (selected.getStartTime() == -1) {
                selected.setStartTime(currentTime);
            }

            selected.setState(ProcessControlBlock.State.RUNNING);

            int start          = currentTime;
            int startRemaining = selected.getRemainingBurst();

            // Run to completion (non-preemptive), apply aging every 4 ms
            while (selected.getRemainingBurst() > 0) {
                currentTime++;
                selected.decrementRemainingBurst();

                // Increment waiting time + timeInQueue for all in ready queue
                for (ProcessControlBlock p : ready) {
                    p.incrementWaitingTime();
                    p.incrementTimeInQueue();
                }

                // Apply aging check every AGING_INTERVAL ms
                if (currentTime % AGING_INTERVAL == 0) {
                    applyStarvationAndAging(ready, currentTime, ready.size());
                }
            }

            selected.setState(ProcessControlBlock.State.TERMINATED);
            selected.setTerminationTime(currentTime);
            selected.setTurnaroundTime(selected.getTerminationTime());        // arrival = 0
            selected.setWaitingTime(selected.getTurnaroundTime() - selected.getBurstTime());

            gantt.add(new GanttEntry(
                    selected.getProcessId(),
                    start,
                    currentTime,
                    startRemaining,
                    0
            ));

            completed.add(selected);
        }

        OutputManager.printResults(completed, gantt, "Priority");
    }

    // =========================================================================
    // STARVATION DETECTION + AGING
    // =========================================================================

   
    private static void applyStarvationAndAging(
            ArrayList<ProcessControlBlock> ready,
            int currentTime,
            int n) {

        if (n == 0) return;

        // Starvation threshold: N x 5 ms (from project spec)
        int starvationThreshold = n * STARVATION_MULT;

        for (ProcessControlBlock p : ready) {

            // timeInQueue = consecutive ms spent waiting in ready queue
            int waitingSoFar = p.getTimeInQueue();

            // -- Starvation detected --
            if (waitingSoFar > starvationThreshold) {

                if (!Sharedresources.starvedProcesses.contains(p.getProcessId())) {
                    Sharedresources.starvedProcesses.add(p.getProcessId());
                    System.out.printf(
                        "[Scheduler] WARNING: Process P%d detected as STARVED " +
                        "(waited %d ms, threshold=%d ms, N=%d)%n",
                        p.getProcessId(), waitingSoFar, starvationThreshold, n
                    );
                }

                // -- Apply aging every AGING_INTERVAL ms --
                if (currentTime % AGING_INTERVAL == 0) {
                    p.applyAging(); // built-in: priority-- (min = 1)
                    System.out.printf(
                        "[Scheduler] Aging applied to P%d - new priority: %d%n",
                        p.getProcessId(), p.getPriority()
                    );
                }
            }
        }
    }

    // HELPER - Select highest priority process (lowest number, tie -> arrival)

    private static ProcessControlBlock getHighestPriorityProcess(
            ArrayList<ProcessControlBlock> ready) {

        ProcessControlBlock best = ready.get(0);

        for (ProcessControlBlock p : ready) {
            if (p.getPriority() < best.getPriority()) {
                best = p;
            } else if (p.getPriority() == best.getPriority()
                    && p.getArrivalTime() < best.getArrivalTime()) {
                best = p;
            }
        }

        return best;
    }

    // =========================================================================
    // OUTPUT - Gantt Chart
    // =========================================================================

    private static void printGantt(ArrayList<GanttEntry> gantt) {

        System.out.println("\n===== Gantt Chart =====");
        System.out.printf("%-12s %-8s %-12s %-12s%n",
                "Time", "Process", "StartBurst", "StopBurst");
        System.out.println("-".repeat(48));

        for (GanttEntry g : gantt) {
            System.out.printf("%-12s %-8s %-12d %-12d%n",
                    g.getStartTime() + "-" + g.getEndTime(),
                    "P" + g.getProcessId(),
                    g.getStartBurst(),
                    g.getStopBurst()
            );
        }
    }

    // =========================================================================
    // OUTPUT - Process Table
    // =========================================================================

    private static void printTable(ArrayList<ProcessControlBlock> processes) {

        System.out.println("\n===== Process Table =====");
        System.out.printf("%-6s %-8s %-8s %-10s %-10s %-12s%n",
                "PID", "Burst", "Start", "Finish", "Waiting", "Turnaround");
        System.out.println("-".repeat(58));

        // Sort by process ID for clean output
        processes.sort(Comparator.comparingInt(ProcessControlBlock::getProcessId));

        for (ProcessControlBlock p : processes) {
            System.out.printf("%-6s %-8d %-8d %-10d %-10d %-12d%n",
                    "P" + p.getProcessId(),
                    p.getBurstTime(),
                    p.getStartTime(),
                    p.getTerminationTime(),
                    p.getWaitingTime(),
                    p.getTurnaroundTime()
            );
        }
    }

    // =========================================================================
    // OUTPUT - Average Metrics
    // =========================================================================

    private static void printAverages(ArrayList<ProcessControlBlock> processes) {

        double totalWT  = 0;
        double totalTAT = 0;

        for (ProcessControlBlock p : processes) {
            totalWT  += p.getWaitingTime();
            totalTAT += p.getTurnaroundTime();
        }

        System.out.printf("%nAverage Waiting Time    : %.2f ms%n", totalWT  / processes.size());
        System.out.printf("Average Turnaround Time : %.2f ms%n",   totalTAT / processes.size());
    }

    // =========================================================================
    // OUTPUT - Starvation Report (Priority only)
    // =========================================================================

    private static void printStarvation() {

        System.out.println("\n===== Starvation Report =====");

        if (Sharedresources.starvedProcesses.isEmpty()) {
            System.out.println("No processes suffered from starvation.");
        } else {
            for (int id : Sharedresources.starvedProcesses) {
                System.out.println("Process P" + id + " suffered from starvation.");
            }
        }
    }

    // =========================================================================
    // HELPER - Deep copy of process list (so original queue is not modified)
    // =========================================================================

    private static ArrayList<ProcessControlBlock> copyProcesses(
            ArrayList<ProcessControlBlock> original) {

        ArrayList<ProcessControlBlock> copy = new ArrayList<>();

        for (ProcessControlBlock p : original) {

            ProcessControlBlock newPCB = new ProcessControlBlock(
                    p.getProcessId(),
                    p.getBurstTime(),
                    p.getOriginalPriority(),   // always copy the original priority
                    p.getMemoryRequired(),
                    p.getArrivalTime()
            );

            copy.add(newPCB);
        }

        return copy;
    }
}
