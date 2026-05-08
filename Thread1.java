package operatingsystemproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class Thread1 implements Runnable {

    private static final String INPUT_FILE      = "job.txt";
    private static final int    MIN_PRIORITY    = 1;
    private static final int    MAX_PRIORITY    = 30;
    private static final int    MIN_BURST       = 1;
    private static final int    MIN_MEMORY      = 1;
    private static final int    MAX_MEMORY      = Sharedresources.TOTAL_MEMORY; // 2048 MB

    private final BlockingQueue<ProcessControlBlock> jobQueue;

    public Thread1(BlockingQueue<ProcessControlBlock> jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        System.out.println("[Thread 1] Started - reading from: " + INPUT_FILE);

        int processesLoaded = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE))) {

            String line;
            int    arrivalOrder = 0; // Used as arrival time (all arrive at time 0, order = file order)

            while ((line = reader.readLine()) != null) {

                // -- Skip blank lines and comment lines (lines starting with #) --
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // -- Parse and validate the line --
                ProcessControlBlock pcb = parseLine(line, arrivalOrder);

                if (pcb == null) {
                    // parseLine already printed the error; skip this line
                    System.err.println("[Thread 1] WARNING: Skipping invalid line -> \"" + line + "\"");
                    continue;
                }

                // -- Insert PCB into Job Queue --
                jobQueue.put(pcb); // Blocks if queue is full (won't happen with unbounded queue)

                System.out.printf("[Thread 1] Loaded -> %s%n", pcb);

                arrivalOrder++;
                processesLoaded++;
            }

        } catch (IOException e) {
            System.err.println("[Thread 1] ERROR: Cannot open file \"" + INPUT_FILE + "\"");
            System.err.println("           Make sure job.txt exists in the working directory.");
            System.err.println("           Details: " + e.getMessage());

        } catch (InterruptedException e) {
            System.err.println("[Thread 1] ERROR: Thread interrupted while inserting into job queue.");
            Thread.currentThread().interrupt();
        }

        // -- Signal Thread 2 that input is done --
        Sharedresources.inputFinished = true;

        System.out.printf("[Thread 1] Finished - %d process(es) added to job queue.%n", processesLoaded);
    }

    private ProcessControlBlock parseLine(String line, int arrivalOrder) {

        try {
            // -- Step 1: Split on ';' to separate memory from the rest --
            // Expected: parts[0] = "1:25:4"   parts[1] = "500"
            String[] parts = line.split(";");

            if (parts.length != 2) {
                System.err.println("[Thread 1] Format error: expected exactly one ';' in -> \"" + line + "\"");
                return null;
            }

            // -- Step 2: Split the left part on ':' --
            // Expected: fields[0] = "1"   fields[1] = "25"   fields[2] = "4"
            String[] fields = parts[0].split(":");

            if (fields.length != 3) {
                System.err.println("[Thread 1] Format error: expected exactly two ':' before ';' in -> \"" + line + "\"");
                return null;
            }

            // -- Step 3: Parse each field as integer --
            int processId      = Integer.parseInt(fields[0].trim());
            int burstTime      = Integer.parseInt(fields[1].trim());
            int priority       = Integer.parseInt(fields[2].trim());
            int memoryRequired = Integer.parseInt(parts[1].trim());

            // -- Step 4: Validate ranges --
            if (!validateFields(processId, burstTime, priority, memoryRequired, line)) {
                return null;
            }

            // -- Step 5: Create and return PCB --
            return new ProcessControlBlock(processId, burstTime, priority, memoryRequired, arrivalOrder);

        } catch (NumberFormatException e) {
            System.err.println("[Thread 1] Parse error: non-integer value found in -> \"" + line + "\"");
            return null;
        }
    }

    private boolean validateFields(int processId, int burstTime,
                                   int priority, int memoryRequired,
                                   String line) {
        boolean valid = true;

        if (processId <= 0) {
            System.err.printf("[Thread 1] Validation error: processId must be > 0, got %d in -> \"%s\"%n",
                              processId, line);
            valid = false;
        }

        if (burstTime < MIN_BURST) {
            System.err.printf("[Thread 1] Validation error: burstTime must be >= %d, got %d in -> \"%s\"%n",
                              MIN_BURST, burstTime, line);
            valid = false;
        }

        if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
            System.err.printf("[Thread 1] Validation error: priority must be %d-%d, got %d in -> \"%s\"%n",
                              MIN_PRIORITY, MAX_PRIORITY, priority, line);
            valid = false;
        }

        if (memoryRequired < MIN_MEMORY || memoryRequired > MAX_MEMORY) {
            System.err.printf("[Thread 1] Validation error: memoryRequired must be %d-%d MB, got %d in -> \"%s\"%n",
                              MIN_MEMORY, MAX_MEMORY, memoryRequired, line);
            valid = false;
        }

        return valid;
    }
}
