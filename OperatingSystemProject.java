
package com.mycompany.operatingsystemproject;


public class OperatingSystemProject {

    public static void main(String[] args) throws InterruptedException {
 
        System.out.println("==============================================");
        System.out.println("           CPU Scheduling Simulator");
        System.out.println("==============================================\n");
 
        // ── Launch Thread 1 (Member 1) ──
        Thread thread1 = new Thread(new Thread1(Sharedresources.jobQueue), "Thread-1-InputManager");
        thread1.start();
 
        // ── Launch Thread 2 (Member 2) — to be implemented by Member 2 ──
        // Thread thread2 = new Thread(new Thread2(SharedResources.jobQueue, SharedResources.readyQueue), "Thread-2-MemoryManager");
        // thread2.start();
 
        // ── Wait for Thread 1 to finish reading the file ──
        thread1.join();
 
        // ── [Member 3] Let user choose scheduling algorithm and run simulation ──
        // Scheduler scheduler = new Scheduler(SharedResources.readyQueue);
        // scheduler.chooseAndRun();
 
        // ── [Member 4] Print output: Gantt chart, table, metrics ──
        // OutputManager.printResults(...);
 
        System.out.println("\n[Main] Simulation complete.");
    }
}