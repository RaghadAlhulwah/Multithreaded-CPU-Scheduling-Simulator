public class MemoryManager {
    private final int TOTAL_MEMORY = 2048;
    private int usedMemory = 0;

    public synchronized boolean hasEnoughMemory(PCB process) {
        return usedMemory + process.getMemoryRequired() <= TOTAL_MEMORY;
    }

    public synchronized void allocateMemory(PCB process) {
        usedMemory += process.getMemoryRequired();
    }

    public synchronized void releaseMemory(PCB process) {
        usedMemory -= process.getMemoryRequired();

        if (usedMemory < 0) {
            usedMemory = 0;
        }
    }

    public synchronized int getUsedMemory() {
        return usedMemory;
    }

    public synchronized int getAvailableMemory() {
        return TOTAL_MEMORY - usedMemory;
    }
}