package operatingsystemproject;


import java.util.Queue;

public class ReadyQueueLoader extends Thread {
    private Queue<ProcessControlBlock> jobQueue;
    private Queue<ProcessControlBlock> readyQueue;
    private MemoryManager memoryManager;

    public ReadyQueueLoader(Queue<ProcessControlBlock> jobQueue,
                            Queue<ProcessControlBlock> readyQueue,
                            MemoryManager memoryManager) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.memoryManager = memoryManager;
    }

    @Override
    public void run() {
        while (true) {
            ProcessControlBlock process = null;

            synchronized (jobQueue) {
                if (!jobQueue.isEmpty()) {
                    process = jobQueue.peek();
                } else {
                    break;
                }
            }

            if (process != null && memoryManager.hasEnoughMemory(process)) {
                synchronized (jobQueue) {
                    jobQueue.poll();
                }

                memoryManager.allocateMemory(process);
                process.setState(ProcessControlBlock.State.READY);

                synchronized (readyQueue) {
                    readyQueue.add(process);
                }
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
