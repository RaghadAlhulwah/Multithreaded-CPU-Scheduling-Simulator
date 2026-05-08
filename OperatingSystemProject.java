package operatingsystemproject;

import java.util.ArrayList;

public class OperatingSystemProject {

    public static void main(String[] args) throws InterruptedException {
 
        System.out.println("==============================================");
        System.out.println("           CPU Scheduling Simulator");
        System.out.println("==============================================\n");
 
        // -- Launch Thread 1 (Member 1) --
        Thread thread1 = new Thread(new Thread1(Sharedresources.jobQueue), "Thread-1-InputManager");
        thread1.start();
 
        // -- Wait for Thread 1 to finish reading the file --
        thread1.join();
        Sharedresources.inputFinished = true;

        // -- Launch Thread 2 (Member 2): move jobs from Job Queue to Ready Queue if memory allows --
        MemoryManager memoryManager = new MemoryManager();
        Thread thread2 = new ReadyQueueLoader(
                Sharedresources.jobQueue,
                Sharedresources.readyQueue,
                memoryManager
        );
        thread2.setName("Thread-2-ReadyQueueLoader");
        thread2.start();
        thread2.join();

        // -- Main thread (Member 3): run selected scheduler, then OutputManager prints results --
        CPUScheduler.showMenuAndRun(new ArrayList<>(Sharedresources.readyQueue));

 
        System.out.println("\n[Main] Simulation complete.");
    }
}
