package CS227;

import java.util.*;

public class CPUScheduler {

    private static final int TIME_QUANTUM = 5;
    private static final int AGING_INTERVAL = 4;

    // ================= MENU =================

    public static void showMenuAndRun(ArrayList<PCB> readyQueue) {

        Scanner input = new Scanner(System.in);

        System.out.println("\n===== CPU Scheduling Simulator =====");

        System.out.println("1. Shortest Job First (SJF)");
        System.out.println("2. Round Robin (RR)");
        System.out.println("3. Priority Scheduling");

        System.out.print("Choose Algorithm: ");

        int choice = input.nextInt();

        ArrayList<PCB> copy = copyProcesses(readyQueue);

        switch (choice) {

            case 1:
                runSJF(copy);
                break;

            case 2:
                runRoundRobin(copy);
                break;

            case 3:
                runPriority(copy);
                break;

            default:
                System.out.println("Invalid Choice");
        }
    }

    // ================= SJF =================

    public static void runSJF(ArrayList<PCB> processes) {

        System.out.println("\n===== SJF Scheduling =====");

        processes.sort((p1, p2) -> {

            if (p1.getBurstTime() != p2.getBurstTime()) {
                return p1.getBurstTime() - p2.getBurstTime();
            }

            return p1.getArrivalOrder() - p2.getArrivalOrder();
        });

        int currentTime = 0;

        ArrayList<GanttEntry> gantt = new ArrayList<>();

        for (PCB p : processes) {

            p.setStartTime(currentTime);
            p.setState("Running");

            int startRemaining = p.getRemainingTime();

            for (int i = 0; i < p.getBurstTime(); i++) {

                currentTime++;

                p.setRemainingTime(
                        p.getRemainingTime() - 1
                );
            }

            p.setTerminationTime(currentTime);

            p.setTurnaroundTime(
                    p.getTerminationTime()
            );

            p.setWaitingTime(
                    p.getTurnaroundTime()
                            - p.getBurstTime()
            );

            p.setState("Terminated");

            gantt.add(
                    new GanttEntry(
                            p.getProcessId(),
                            p.getStartTime(),
                            p.getTerminationTime(),
                            startRemaining,
                            0
                    )
            );
        }

        printGantt(gantt);

        printTable(processes);

        printAverages(processes);
    }

    // ================= ROUND ROBIN =================

    public static void runRoundRobin(ArrayList<PCB> processes) {

        System.out.println("\n===== Round Robin =====");

        Queue<PCB> queue = new LinkedList<>(processes);

        ArrayList<GanttEntry> gantt = new ArrayList<>();

        int currentTime = 0;

        while (!queue.isEmpty()) {

            PCB p = queue.poll();

            if (p.getStartTime() == -1) {
                p.setStartTime(currentTime);
            }

            p.setState("Running");

            int start = currentTime;

            int startRemaining = p.getRemainingTime();

            int executedTime =
                    Math.min(TIME_QUANTUM,
                            p.getRemainingTime());

            // simulate 1 ms

            for (int i = 0; i < executedTime; i++) {

                currentTime++;

                p.setRemainingTime(
                        p.getRemainingTime() - 1
                );
            }

            gantt.add(
                    new GanttEntry(
                            p.getProcessId(),
                            start,
                            currentTime,
                            startRemaining,
                            p.getRemainingTime()
                    )
            );

            if (p.getRemainingTime() > 0) {

                p.setState("Ready");

                queue.add(p);

            } else {

                p.setState("Terminated");

                p.setTerminationTime(currentTime);

                p.setTurnaroundTime(
                        p.getTerminationTime()
                );

                p.setWaitingTime(
                        p.getTurnaroundTime()
                                - p.getBurstTime()
                );
            }
        }

        printGantt(gantt);

        printTable(processes);

        printAverages(processes);
    }

    // ================= PRIORITY =================

    public static void runPriority(ArrayList<PCB> processes) {

        System.out.println("\n===== Priority Scheduling =====");

        ArrayList<PCB> ready =
                new ArrayList<>(processes);

        ArrayList<PCB> completed =
                new ArrayList<>();

        ArrayList<GanttEntry> gantt =
                new ArrayList<>();

        ArrayList<Integer> starvedProcesses =
                new ArrayList<>();

        int currentTime = 0;

        while (!ready.isEmpty()) {

            applyStarvationAndAging(
                    ready,
                    currentTime,
                    starvedProcesses
            );

            PCB selected =
                    getHighestPriorityProcess(ready);

            ready.remove(selected);

            if (selected.getStartTime() == -1) {
                selected.setStartTime(currentTime);
            }

            selected.setState("Running");

            int start = currentTime;

            int startRemaining =
                    selected.getRemainingTime();

            while (selected.getRemainingTime() > 0) {

                currentTime++;

                selected.setRemainingTime(
                        selected.getRemainingTime() - 1
                );

                if (currentTime % AGING_INTERVAL == 0) {

                    applyStarvationAndAging(
                            ready,
                            currentTime,
                            starvedProcesses
                    );
                }
            }

            selected.setState("Terminated");

            selected.setTerminationTime(currentTime);

            selected.setTurnaroundTime(
                    selected.getTerminationTime()
            );

            selected.setWaitingTime(
                    selected.getTurnaroundTime()
                            - selected.getBurstTime()
            );

            gantt.add(
                    new GanttEntry(
                            selected.getProcessId(),
                            start,
                            currentTime,
                            startRemaining,
                            0
                    )
            );

            completed.add(selected);
        }

        printGantt(gantt);

        printTable(completed);

        printAverages(completed);

        System.out.println("\n===== Starvation =====");

        if (starvedProcesses.isEmpty()) {

            System.out.println(
                    "No starved processes."
            );

        } else {

            for (int id : starvedProcesses) {

                System.out.println(
                        "Process P" + id
                                + " suffered from starvation."
                );
            }
        }
    }

    // ================= AGING =================

    private static void applyStarvationAndAging(

            ArrayList<PCB> ready,
            int currentTime,
            ArrayList<Integer> starvedProcesses) {

        int n = ready.size();

        if (n == 0) {
            return;
        }

        for (PCB p : ready) {

            int waitingSoFar =
                    currentTime
                            - p.getReadyEnterTime();

            // starvation condition

            if (waitingSoFar > n * 5) {

                if (!starvedProcesses.contains(
                        p.getProcessId())) {

                    starvedProcesses.add(
                            p.getProcessId()
                    );
                }

                // aging every 4 ms

                if (currentTime % AGING_INTERVAL == 0
                        && p.getPriority() > 1) {

                    p.setPriority(
                            p.getPriority() - 1
                    );
                }
            }
        }
    }

    // ================= PRIORITY SELECT =================

    private static PCB getHighestPriorityProcess(
            ArrayList<PCB> ready) {

        PCB best = ready.get(0);

        for (PCB p : ready) {

            if (p.getPriority()
                    < best.getPriority()) {

                best = p;

            } else if (
                    p.getPriority()
                            == best.getPriority()

                            &&

                            p.getArrivalOrder()
                                    < best.getArrivalOrder()
            ) {

                best = p;
            }
        }

        return best;
    }

    // ================= GANTT =================

    private static void printGantt(
            ArrayList<GanttEntry> gantt) {

        System.out.println("\n===== Gantt Chart =====");

        System.out.println(
                "Time\tProcess\tStartBurst\tStopBurst"
        );

        for (GanttEntry g : gantt) {

            System.out.println(

                    g.getStartTime()
                            + "-"
                            + g.getEndTime()

                            + "\tP"

                            + g.getProcessId()

                            + "\t"

                            + g.getStartBurst()

                            + "\t\t"

                            + g.getStopBurst()
            );
        }
    }

    // ================= TABLE =================

    private static void printTable(
            ArrayList<PCB> processes) {

        System.out.println(
                "\n===== Process Table ====="
        );

        System.out.println(
                "PID\tBurst\tStart\tFinish\tWaiting\tTurnaround"
        );

        processes.sort(
                Comparator.comparingInt(
                        PCB::getProcessId
                )
        );

        for (PCB p : processes) {

            System.out.println(

                    "P"
                            + p.getProcessId()

                            + "\t"

                            + p.getBurstTime()

                            + "\t"

                            + p.getStartTime()

                            + "\t"

                            + p.getTerminationTime()

                            + "\t"

                            + p.getWaitingTime()

                            + "\t"

                            + p.getTurnaroundTime()
            );
        }
    }

    // ================= AVERAGES =================

    private static void printAverages(
            ArrayList<PCB> processes) {

        double totalWT = 0;

        double totalTAT = 0;

        for (PCB p : processes) {

            totalWT += p.getWaitingTime();

            totalTAT += p.getTurnaroundTime();
        }

        System.out.printf(
                "\nAverage Waiting Time: %.2f ms\n",
                totalWT / processes.size()
        );

        System.out.printf(
                "Average Turnaround Time: %.2f ms\n",
                totalTAT / processes.size()
        );
    }

    // ================= COPY =================

    private static ArrayList<PCB> copyProcesses(
            ArrayList<PCB> original) {

        ArrayList<PCB> copy =
                new ArrayList<>();

        for (PCB p : original) {

            PCB newPCB = new PCB(

                    p.getProcessId(),

                    p.getBurstTime(),

                    p.getPriority(),

                    p.getMemoryRequired(),

                    p.getArrivalOrder()
            );

            newPCB.setReadyEnterTime(
                    p.getReadyEnterTime()
            );

            copy.add(newPCB);
        }

        return copy;
    }
}