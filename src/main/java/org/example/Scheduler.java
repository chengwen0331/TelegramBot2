package org.example;

/**
 * The Scheduler class represents a scheduler that uses the Round Robin algorithm.
 * It manages the execution of processes.
 */
public class Scheduler {
    private int numOfProcess;
    private int quantum;
    private int burstTime[];
    private int arrivalTime[];
    private String processID [];
    private int timer;
    private int maxProcessIndex;
    private int rem_burstTime[];
    private int queue[];
    private boolean complete[];
    private int completeTime[];
    private int [] startTime;
    private boolean [] startTimeSet;

    /**
     * Initializes a Scheduler object with the specified parameters.
     *
     * @param numOfProcess The total number of processes.
     * @param quantumNum The time quantum for the Round Robin scheduling algorithm.
     * @param processID An array containing the process IDs.
     * @param burstTime An array containing the burst times for each process.
     * @param arrivalTime An array containing the arrival times for each process.
     */
    public Scheduler(int numOfProcess, int quantumNum, String [] processID, int [] burstTime, int [] arrivalTime) {
        // Constructor
        this.numOfProcess = numOfProcess;
        this.quantum = quantumNum;
        this.processID = processID;
        this.arrivalTime = arrivalTime;
        this.timer = 0;
        this.maxProcessIndex = 0;
        this.rem_burstTime = burstTime;
        this.queue = new int[numOfProcess];
        this.complete = new boolean[numOfProcess];
        this.completeTime = new int[numOfProcess];
        this.startTime = new int[numOfProcess];
        this.startTimeSet = new boolean[numOfProcess];
    }

    /**
     * This method is to run the Round Robin scheduling algorithm for a set of processes.
     */
    public void runScheduler() {
        // Implement Round Robin scheduling algorithm
        for(int i = 0; i < numOfProcess; i++){
            complete[i] = false; //all process incomplete
            queue[i] = 0; //no process is initially scheduled.
            startTimeSet[i] = false;
        }
        while(timer < arrivalTime[maxProcessIndex]) {   //Incrementing Timer until the first process arrives

            timer++;
        }

        queue[0] = 1; // Schedule the first process in the queue
        while(true){
            // Check if all processes are completed
            boolean flag = true;
            for(int i = 0; i < numOfProcess; i++){
                if(rem_burstTime[i] != 0){
                    flag = false;
                    break;
                }
            }
            if(flag) {
                break;
            }
            // Execute processes in the queue
            for(int i = 0; (i < numOfProcess) && (queue[i] != 0); i++){
                int ctr = 0; //counter value to keep track of how much time has been spent on the current process within the time quantum
                // Check if start time has already been set for the process
                if (startTimeSet[i] == false) {
                    System.out.println(startTimeSet[i]);
                    // Set start time only once
                    startTime[i] = timer;
                    startTimeSet[i] = true;
                }
                // Execute the process within the time quantum
                if (hasArrivals(arrivalTime)) {
                    while((ctr < quantum) && (rem_burstTime[queue[0]-1] > 0)){
                        rem_burstTime[queue[0]-1] -= 1; //runs until either the process completes its burst
                        timer += 1;
                        ctr++;

                        //Updating the ready queue until all the processes arrive
                        checkNewArrival(timer, arrivalTime, numOfProcess, maxProcessIndex, queue);
                    }
                    // Update completion time and maintain the queue
                    if((rem_burstTime[queue[0]-1] == 0) && (complete[queue[0]-1] == false)){
                        completeTime[queue[0]-1] = timer;        //turn currently stores exit times
                        complete[queue[0]-1] = true;
                    }
                    queueMaintainence(queue,numOfProcess);
                }
                else{
                    // Handle the case when all arrival times are zero
                    if (rem_burstTime[i] > 0 && arrivalTime[i] <= timer) {
                        if (rem_burstTime[i] > quantum) {
                            System.out.println(queue[i]);
                            timer += quantum;
                            rem_burstTime[i] -= quantum;
                        }
                        else {
                            timer += rem_burstTime[i];
                            rem_burstTime[i] = 0;
                            completeTime[i] = timer;
                        }
                    }
                    queueUpdation(queue, timer, arrivalTime, numOfProcess, maxProcessIndex);

                }
            }
        }
    }

    /**
     * This method is to update the process queue based on the arrival of new processes.
     *
     * @param queue             The array representing the process queue.
     * @param timer             The current timer value.
     * @param arrival           The array containing arrival times of processes.
     * @param numOfProcess      The total number of processes.
     * @param maxProccessIndex   The index of the process with the maximum arrival time.
     */
    public void queueUpdation(int queue[],int timer,int arrival[],int numOfProcess, int maxProccessIndex){
        int zeroIndex = -1; //a flag indicating whether an available slot (zero) has been found in the queue array
        // Check for an available slot in the queue
        for(int i = 0; i < numOfProcess; i++){
            if(queue[i] == 0){
                zeroIndex = i; // Found an available slot
                break;
            }
        }
        if(zeroIndex == -1)
            return; // No available slot found, exit without updating the queue
        queue[zeroIndex] = maxProccessIndex + 1; // Update the queue with the index of the newly arrived process
    }

    /**
     * This method is to check for the arrival of new processes and updates the process queue accordingly.
     *
     * @param timer Current time in the scheduling algorithm.
     * @param arrival Array containing arrival times of processes.
     * @param numOfProcess Total number of processes in the system.
     * @param maxProccessIndex Index of the last processed or scheduled process.
     * @param queue Array representing the process queue.
     */
    public void checkNewArrival(int timer, int arrival[], int numOfProcess, int maxProccessIndex,int queue[]){

        boolean newArrival = false; //still processes that have not arrived yet
        for (int j = (maxProccessIndex + 1); j < numOfProcess; j++) {
            if (arrival[j] <= timer) { //checks if the arrival time of the process at index j is less than or equal to the current time. If true, it means that the process has arrived
                if (!isProcessInQueue(j + 1, queue) && maxProccessIndex < j) {
                    maxProccessIndex = j; //If true, it updates maxProccessIndex to the new index.
                    newArrival = true;
                }
            }
        }
        if (newArrival) {   //adds the index of the arriving process(if any)
            queueUpdation(queue, timer, arrival, numOfProcess, maxProccessIndex);

        }

    }

    /**
     * This method is to maintain the integrity of the process queue by shifting elements to the left.
     *
     * @param queue Array representing the process queue.
     * @param numOfProcess Total number of processes in the system.
     */
    public void queueMaintainence(int queue[], int numOfProcess){
        //Ensures that the loop index (i) does not go beyond the last valid index of the queue array.
        //Ensures that the next element in the queue is not 0, indicating the end of the active processes in the queue.
        for(int i = 0; (i < numOfProcess-1) && (queue[i+1] != 0) ; i++){
            int temp = queue[i]; // temporary variable used for swapping
            queue[i] = queue[i+1];
            queue[i+1] = temp;
        }
    }

    public int[] getTurnaroundTime() {
        return completeTime;
    }

    /**
     * This method is to retrieve the array containing the start times for each process.
     *
     * @return An array of integers representing the start times for each process.
     */
    public int[] getStartTime() {
        return startTime;
    }

    /**
     * This method is to check if there are multiple arrival times.
     *
     * @param arrivalTime   The array containing arrival times of processes.
     * @return              True if there are multiple arrival times, indicating new arrivals; otherwise, false.
     */
    private boolean hasArrivals(int arrivalTime[]) {
        for (int i = 0; i < numOfProcess - 1; i++) {
            if (arrivalTime[i] != arrivalTime[i+1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is to check if a process with a specific index is present in the given process queue.
     *
     * @param processIndex  The index of the process to check.
     * @param queue         The array representing the process queue.
     * @return              True if the process is in the queue; otherwise, false.
     */
    public static boolean isProcessInQueue(int processIndex, int queue[]) {
        for (int i : queue) {
            if (i == processIndex) {
                return true;
            }
        }
        return false;
    }

}
