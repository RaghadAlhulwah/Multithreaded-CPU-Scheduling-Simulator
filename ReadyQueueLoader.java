import java.util.Queue;

public class ReadyQueueLoader extends Thread {
    private Queue<PCB> jobQueue;
    private Queue<PCB> readyQueue;
    private MemoryManager memoryManager;

    public ReadyQueueLoader(Queue<PCB> jobQueue,
                            Queue<PCB> readyQueue,
                            MemoryManager memoryManager) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.memoryManager = memoryManager;
    }

    @Override
    public void run() {
        while (true) {
            PCB process = null;

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
                process.setState("READY");

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