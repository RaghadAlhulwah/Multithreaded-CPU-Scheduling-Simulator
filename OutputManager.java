package operatingsystemproject;

import java.util.List;

public class OutputManager {

    public static void printResults(List<ProcessControlBlock> processes,
                                    List<GanttEntry> ganttChart,
                                    String algorithmName) {

        System.out.println("\n==================================================");
        System.out.println("Scheduling Algorithm: " + algorithmName);
        System.out.println("==================================================");

        printGanttChart(ganttChart);
        printProcessTable(processes);
        printMetrics(processes);

        if (algorithmName.equalsIgnoreCase("Priority")
                || algorithmName.equalsIgnoreCase("Priority Scheduling")) {
            printStarvedProcesses();
        }
    }

    public static void printGanttChart(List<GanttEntry> ganttChart) {
        System.out.println("\n===== Gantt Chart =====");

        if (ganttChart == null || ganttChart.isEmpty()) {
            System.out.println("No execution data available.");
            return;
        }

        System.out.println("\nExecution Order:");

        for (GanttEntry entry : ganttChart) {
            System.out.print("| P" + entry.getProcessId() + " ");
        }
        System.out.println("|");

        for (GanttEntry entry : ganttChart) {
            System.out.print(entry.getStartTime() + "    ");
        }

        System.out.println(ganttChart.get(ganttChart.size() - 1).getEndTime());

        System.out.println("\nDetailed Gantt Chart:");
        System.out.printf("%-12s %-10s %-15s %-15s%n",
                "Time", "Process", "Start Burst", "Stop Burst");
        System.out.println("-------------------------------------------------------");

        for (GanttEntry entry : ganttChart) {
            System.out.printf("%-12s %-10s %-15d %-15d%n",
                    entry.getStartTime() + "-" + entry.getEndTime(),
                    "P" + entry.getProcessId(),
                    entry.getStartBurst(),
                    entry.getStopBurst());
        }
    }

    public static void printProcessTable(List<ProcessControlBlock> processes) {
        System.out.println("\n===== Process Table =====");

        if (processes == null || processes.isEmpty()) {
            System.out.println("No process data available.");
            return;
        }

        System.out.printf("%-12s %-12s %-12s %-15s %-15s %-15s%n",
                "Process ID", "Burst", "Start", "Termination", "Waiting", "Turnaround");
        System.out.println("--------------------------------------------------------------------------------");

        for (ProcessControlBlock p : processes) {
            System.out.printf("%-12s %-12d %-12d %-15d %-15d %-15d%n",
                    "P" + p.getProcessId(),
                    p.getBurstTime(),
                    p.getStartTime(),
                    p.getTerminationTime(),
                    p.getWaitingTime(),
                    p.getTurnaroundTime());
        }
    }

    public static void printMetrics(List<ProcessControlBlock> processes) {
        System.out.println("\n===== Performance Metrics =====");

        if (processes == null || processes.isEmpty()) {
            System.out.println("No processes available for metrics.");
            return;
        }

        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (ProcessControlBlock p : processes) {
            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();
        }

        double averageWaiting = totalWaiting / processes.size();
        double averageTurnaround = totalTurnaround / processes.size();

        System.out.printf("Average Waiting Time    : %.2f ms%n", averageWaiting);
        System.out.printf("Average Turnaround Time : %.2f ms%n", averageTurnaround);
    }

    public static void printStarvedProcesses() {
        System.out.println("\n===== Starvation Report =====");

        if (Sharedresources.starvedProcesses == null || Sharedresources.starvedProcesses.isEmpty()) {
            System.out.println("No processes suffered from starvation.");
            return;
        }

        for (Integer processId : Sharedresources.starvedProcesses) {
            System.out.println("Process P" + processId + " suffered from starvation.");
        }
    }
}        if (Sharedresources.starvedProcesses.isEmpty()) {
            System.out.println("No starved processes.");
            return;
        }

        for (Integer processId : Sharedresources.starvedProcesses) {
            System.out.println("P" + processId);
        }
    }
}
