/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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

        if (algorithmName.equalsIgnoreCase("Priority")) {
            printStarvedProcesses();
        }
    }

    public static void printGanttChart(List<GanttEntry> ganttChart) {
        System.out.println("\nGantt Chart:");

        if (ganttChart == null || ganttChart.isEmpty()) {
            System.out.println("No execution data available.");
            return;
        }

        for (GanttEntry entry : ganttChart) {
            System.out.print("| P" + entry.getProcessId() + " ");
        }
        System.out.println("|");

        for (GanttEntry entry : ganttChart) {
            System.out.print(entry.getStartTime() + "    ");
        }

        System.out.println(ganttChart.get(ganttChart.size() - 1).getEndTime());
    }

    public static void printProcessTable(List<ProcessControlBlock> processes) {
        System.out.println("\nProcess Table:");

        System.out.printf("%-12s %-12s %-12s %-15s %-15s %-15s%n",
                "Process ID", "Burst", "Start", "Termination", "Waiting", "Turnaround");

        for (ProcessControlBlock p : processes) {
            System.out.printf("%-12d %-12d %-12d %-15d %-15d %-15d%n",
                    p.getProcessId(),
                    p.getBurstTime(),
                    p.getStartTime(),
                    p.getTerminationTime(),
                    p.getWaitingTime(),
                    p.getTurnaroundTime());
        }
    }

    public static void printMetrics(List<ProcessControlBlock> processes) {
        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (ProcessControlBlock p : processes) {
            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();
        }

        double avgWaiting = totalWaiting / processes.size();
        double avgTurnaround = totalTurnaround / processes.size();

        System.out.println("\nPerformance Metrics:");
        System.out.printf("Average Waiting Time: %.2f ms%n", avgWaiting);
        System.out.printf("Average Turnaround Time: %.2f ms%n", avgTurnaround);
    }

    public static void printStarvedProcesses() {
        System.out.println("\nStarved Processes:");

        if (Sharedresources.starvedProcesses.isEmpty()) {
            System.out.println("No starved processes.");
            return;
        }

        for (Integer processId : Sharedresources.starvedProcesses) {
            System.out.println("P" + processId);
        }
    }
}