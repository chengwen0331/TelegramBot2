package org.example;

public class Result {
    private int numOfProcess;
    private int executeTime[];
    private int responseTime[];
    private int burstTime[];
    private int arrivalTime[];
    private int turnTime[];
    private int waitTime[];
    private float avgWait = 0;
    private float avgTurn = 0;
    private float avgResponse = 0;

    public Result(int numOfProcess, int burstTime[], int arrivalTime[], int turnaroundTime[], int startTime[]) {
        this.numOfProcess = numOfProcess;
        this.executeTime = startTime;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.turnTime = turnaroundTime;
        this.waitTime = new int[numOfProcess];
        this.responseTime = new int[numOfProcess];
        // Implement if needed
    }

    public void calculateResults() {
        for(int i = 0; i < numOfProcess; i++){
            responseTime[i] = executeTime[i] - arrivalTime[i];
            turnTime[i] = turnTime[i] - arrivalTime[i];
            waitTime[i] = turnTime[i] - burstTime[i];
            avgTurn += turnTime[i];
            avgWait += waitTime[i];
            avgResponse += responseTime[i];
        }
        avgWait/=numOfProcess;
        avgTurn/=numOfProcess;
        avgResponse/=numOfProcess;
    }

    public void displayResults() {
        System.out.print("\nProgram No.\tArrival Time\tBurst Time\tWait Time\tTurnAround Time"
                + "\n");
        for(int i = 0; i < numOfProcess; i++){
            System.out.print(i+1+"\t\t"+arrivalTime[i]+"\t\t"+burstTime[i]+"\t\t"+responseTime[i]
                    +"\t\t"+waitTime[i]+"\t\t"+turnTime[i]+ "\n");
        }
        System.out.print("\nAverage wait time : "+(avgWait)
                +"\nAverage Turn Around Time : "+(avgTurn)
                +"\nAverage Response Time : "+(avgResponse));
    }

    public float getAvgTurnTime(){
        return Float.parseFloat(String.format("%.2f", avgTurn));
    }

    public float getAvgWaitTime(){
        return Float.parseFloat(String.format("%.2f", avgWait));
    }

    public float getAvgResTime(){
        return Float.parseFloat(String.format("%.2f", avgResponse));
    }

    public int [] getWaitTime(){
        return waitTime;
    }

    public int [] getTurnTime(){
        return turnTime;
    }

    public int [] getResponseTime(){
        return responseTime;
    }
}
