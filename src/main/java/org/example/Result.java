package org.example;

/**
 * Represents the result of a scheduling algorithm for a set of processes.
 */
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

    /**
     * Constructs a Result object with information about the processes.
     *
     * @param numOfProcess   The number of processes.
     * @param burstTime      The burst time of each process.
     * @param arrivalTime    The arrival time of each process.
     * @param turnaroundTime The turnaround time of each process.
     * @param startTime      The start time of each process.
     */
    public Result(int numOfProcess, int burstTime[], int arrivalTime[], int turnaroundTime[], int startTime[]) {
        this.numOfProcess = numOfProcess;
        this.executeTime = startTime;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.turnTime = turnaroundTime;
        this.waitTime = new int[numOfProcess];
        this.responseTime = new int[numOfProcess];
    }

    /**
     * This method is to calculate and update the results based on the provided information.
     */
    public void calculateResults() {
        for(int i = 0; i < numOfProcess; i++){
            System.out.println("Burst Time is " + burstTime[i]);
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

    /**
     * This method is to display the results of the scheduling process, including program details, execution times, and averages for debugging.
     */
    public void displayResults() {
        System.out.print("\nProgram No.\tArrival Time\tBurst Time\tStart Time\tResponse Time\tWait Time\tTurnAround Time"
                + "\n");
        for(int i = 0; i < numOfProcess; i++){
            System.out.print(i+1+"\t\t"+arrivalTime[i]+"\t\t"+burstTime[i]+"\t\t"+executeTime[i]+"\t\t"+responseTime[i]
                    +"\t\t"+waitTime[i]+"\t\t"+turnTime[i]+ "\n");
        }
        System.out.print("\nAverage wait time : "+(avgWait)
                +"\nAverage Turn Around Time : "+(avgTurn)
                +"\nAverage Response Time : "+(avgResponse));
    }

    /**
     * This method is to get the average turnaround time.
     *
     * @return The average turnaround time.
     */
    public float getAvgTurnTime(){
        return Float.parseFloat(String.format("%.2f", avgTurn));
    }

    /**
     * This method is to get the average waiting time.
     *
     * @return The average waiting time.
     */
    public float getAvgWaitTime(){
        return Float.parseFloat(String.format("%.2f", avgWait));
    }

    /**
     * This method is to get the average response time.
     *
     * @return The average response time.
     */
    public float getAvgResTime(){
        return Float.parseFloat(String.format("%.2f", avgResponse));
    }

    /**
     * This method is to get the array of waiting times.
     *
     * @return An array containing the waiting times for each process.
     */
    public int [] getWaitTime(){
        return waitTime;
    }

    /**
     * This method is to get the array of turnaround times.
     *
     * @return An array containing the turnaround times for each process.
     */
    public int [] getTurnTime(){
        return turnTime;
    }

    /**
     * This method is to get the array of response times.
     *
     * @return An array containing the response times for each process.
     */
    public int [] getResponseTime(){
        return responseTime;

    }
}
