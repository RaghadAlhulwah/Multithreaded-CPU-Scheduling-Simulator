package com.mycompany.operatingsystemproject;

public class PCB {

    private int processId;
    private String state;
    private int burstTime;
    private int remainingTime;
    private int priority;
    private int memoryRequired;

    private int arrivalOrder;
    private int readyEnterTime;

    private int startTime;
    private int terminationTime;
    private int waitingTime;
    private int turnaroundTime;

    public PCB(int processId, int burstTime, int priority,
               int memoryRequired, int arrivalOrder) {

        this.processId = processId;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.memoryRequired = memoryRequired;
        this.arrivalOrder = arrivalOrder;

        this.state = "Ready";
        this.readyEnterTime = 0;
        this.startTime = -1;
        this.terminationTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }

    public int getProcessId() {
        return processId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMemoryRequired() {
        return memoryRequired;
    }

    public int getArrivalOrder() {
        return arrivalOrder;
    }

    public int getReadyEnterTime() {
        return readyEnterTime;
    }

    public void setReadyEnterTime(int readyEnterTime) {
        this.readyEnterTime = readyEnterTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(int terminationTime) {
        this.terminationTime = terminationTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }
}
