package org.example;
/**
 * Import statements for required Java and Telegram API classes.
 */
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static int operationID;
    static String [] processID;
    static int [] burstTime;
    static int [] arrivalTime;
    static int [] waitTime;
    static int [] turnTime;
    static int [] responseTime;
    static float averageWait = 0;
    static float averageResponse = 0;
    static float averageTurn = 0;
    static int data; // Placeholder for retrieved data from the database
    static int id;
    static List<String> processIDList = new ArrayList<>(); // List to store process ID
    static List<Integer> burstTimeList = new ArrayList<>(); // List to store burst times
    static List<Integer> arrivalTimeList = new ArrayList<>(); // List to store arrival times
    static List<Integer> responseTimeList = new ArrayList<>(); // List to store response times
    static Connection conn = connect(); // Database connection instance

    /**
     * The main method to start the application.
     *
     * - Initiates a connection to the SQLite database.
     * - Generates the required tables using the CreateTable class.
     * - Registers the Telegram bot using the simpleBot class.
     * - Adds a shutdown hook to ensure the database connection is closed upon program termination.
     *
     * @param args Command-line arguments (not used in this application).
     * @throws TelegramApiException If there is an issue with the Telegram API.
     */
    public static void main(String[] args) throws TelegramApiException {
        // Establish a connection to the SQLite database
        connect();
        // Create an instance of the CreateTable class to generate tables if they don't exist
        CreateTable generate = new CreateTable();
        generate.createNewTable();

        try {
            // Register the Telegram bot using the TelegramBotsApi
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new simpleBot());
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Print the stack trace if an exception occurs during bot registration
        }

        // Add a shutdown hook to close the database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Connection to SQLite has been closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * This method is to process the input related to the number of processes and quantum number.
     *
     * Inserts the specified values into the 'processnum_data' table in the SQLite database.
     *
     * @param numOfProcess The number of processes to be processed.
     * @param quantumNum   The quantum number to be processed.
     */
    public static void processInput(int numOfProcess, int quantumNum) {

        String sql = "INSERT INTO processnum_data(numOfProcess, quantumNum) VALUES(?,?)";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql); // Create a PreparedStatement to execute the SQL query
            // Set the values for the placeholders in the SQL query
            pstmt.setInt(1, numOfProcess);
            pstmt.setInt(2, quantumNum);
            pstmt.executeUpdate(); // Execute the SQL query to insert data into the table
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method is to handle the input related to individual processes.
     *
     * Inserts the specified values into the 'process_data' table in the SQLite database.
     * The operationID is obtained by querying the 'processnum_data' table based on the number of processes.
     *
     * @param numOfProcess The total number of processes involved in the operation.
     * @param processID    The unique identifier for the current process.
     * @param burstTime     The time required for the current process to complete its execution (burst time).
     * @param arrivalTime   The time at which the current process arrives for execution.
     */
    public static void handleInput(int numOfProcess, String processID, int burstTime, int arrivalTime) {
        data = selectData(numOfProcess); // Obtain the operationID based on the number of processes
        String sql = "INSERT INTO process_data(operationID, processID, burstTime, arrivalTime) VALUES(?,?,?,?)";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, data);
            pstmt.setString(2, processID);
            pstmt.setInt(3, burstTime);
            pstmt.setInt(4, arrivalTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method is to select the operationID from the 'processnum_data' table based on the number of processes.
     *
     * Executes a SQL query to retrieve the operationID from the 'processnum_data' table,
     * ordering the results by operationID in descending order and limiting the result set to 1.
     *
     * @param number The number of processes for which the operationID needs to be retrieved.
     * @return The retrieved operationID based on the specified number of processes.
     */
    public static int selectData(int number) {
        int data = 0;
        String sql = "SELECT operationID FROM processnum_data WHERE numOfProcess = ? ORDER BY operationID DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, number);
            ResultSet rs = pstmt.executeQuery();

            // Check if there is a result before trying to retrieve data
            if (rs.next()) {
                data = rs.getInt("operationID");
                System.out.println("Operation ID is" + data);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    /**
     * This method is to select all process data from the 'process_data' table for a specific operationID.
     *
     * Executes a SQL query to retrieve all rows from the 'process_data' table where
     * the operationID matches the specified data. The retrieved data is then processed
     * and stored in corresponding arrays for further use.
     *
     * @param num The number of processes to be retrieved.
     * @param quantum The quantum number associated with the operation.
     */
    public static void selectAll(int num, int quantum){
        String sql = "SELECT * FROM process_data WHERE operationID = ?";
        processID= new String[num];
        burstTime= new int[num];
        arrivalTime= new int[num];
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, data);
                ResultSet rs = pstmt.executeQuery(); // Execute the SQL query and obtain the result set
                // Clear existing lists to avoid duplicate data
                processIDList.clear();
                burstTimeList.clear();
                arrivalTimeList.clear();

                // Iterate through the result set and retrieve process data
                while (rs.next()) {
                    operationID = rs.getInt("operationID");
                    String currentProcessID = rs.getString("processID");
                    processIDList.add(currentProcessID);
                    int currentBurstTime = rs.getInt("burstTime");
                    burstTimeList.add(currentBurstTime);
                    int currentArrivalTime = rs.getInt("arrivalTime");
                    arrivalTimeList.add(currentArrivalTime);
                }

                // Convert lists to arrays for further processing
                processID = processIDList.toArray(new String[0]);
                burstTime = burstTimeList.stream().mapToInt(Integer::intValue).toArray();
                arrivalTime = arrivalTimeList.stream().mapToInt(Integer::intValue).toArray();
                processRetrievedData(data, num, quantum, processID, burstTime, arrivalTime);
            } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method is to process the retrieved data, performs scheduling and result calculation,
     * and update the results in the database.
     *
     * This method takes the retrieved data, creates a Scheduler instance, and runs
     * the scheduling algorithm. It then calculates results using a Result instance
     * and displays the results. The average wait, turnaround, and response times
     * along with individual process wait, turnaround, and response times are stored.
     * The final results are then updated in the database using the updateResult method.
     *
     * @param operationID The unique identifier for the current operation.
     * @param numOfProcess The number of processes to be scheduled.
     * @param quantumNum The quantum number used in the scheduling algorithm.
     * @param processID An array containing process IDs.
     * @param burstTime An array containing burst times for each process.
     * @param arrivalTime An array containing arrival times for each process.
     */
    public static void processRetrievedData(int operationID, int numOfProcess, int quantumNum, String [] processID, int [] burstTime, int [] arrivalTime) {
        // Create a temporary array to store burst times before scheduling
        int[] tem_burstTime = new int[numOfProcess];
        for(int i = 0; i < numOfProcess; i++){
            tem_burstTime[i] = burstTime[i];
        }
        Scheduler scheduler = new Scheduler(numOfProcess, quantumNum, processID, burstTime, arrivalTime);
        scheduler.runScheduler(); // Run the scheduling algorithm
        Result resultCalculator = new Result(numOfProcess, tem_burstTime, arrivalTime, scheduler.getTurnaroundTime(), scheduler.getStartTime());
        resultCalculator.calculateResults();
        resultCalculator.displayResults();
        // Update average wait time, average turnaround time, and average response time
        averageWait = resultCalculator.getAvgWaitTime();
        averageTurn = resultCalculator.getAvgTurnTime();
        averageResponse = resultCalculator.getAvgResTime();
        // Get individual process wait, turnaround, and response times
        waitTime = resultCalculator.getWaitTime();
        turnTime = resultCalculator.getTurnTime();
        responseTime = resultCalculator.getResponseTime();
        handleResult(operationID, averageWait, averageTurn, averageResponse); // Handle and update the results in the database
        id = retrieveID(operationID);
        // Update individual process results in the database
        for(int i = 0; i < numOfProcess; i++){
            int response = responseTime[i];
            int wait = waitTime[i];
            int turn = turnTime[i];
            updateResult(id, operationID, response, wait, turn);
            id++;
        }
    }

    /**
     * This method is to handle the results of a scheduling operation and updates the database.
     *
     * This method takes the operationID along with the average wait, turnaround, and
     * response times. It then inserts these results into the 'process_result' table in
     * the database.
     *
     * @param operationID The unique identifier for the current operation.
     * @param averageWait The average waiting time for all processes.
     * @param averageTurn The average turnaround time for all processes.
     * @param averageResponse The average response time for all processes.
     */
    public static void handleResult(int operationID, float averageWait, float averageTurn, float averageResponse) {
        String sql = "INSERT INTO process_result(operationID, avgResponse, avgWaiting, avgTurnaround) VALUES(?,?,?,?)";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, operationID);
            pstmt.setFloat(2, averageResponse);
            pstmt.setFloat(3, averageWait);
            pstmt.setFloat(4, averageTurn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method is to update the result data for a specific process in the 'process_data' table.
     *
     * This method takes the unique identifier (id) of a process, along with the operationID,
     * response time, waiting time, and turnaround time. It then updates the corresponding row
     * in the 'process_data' table with the new values.
     *
     * @param id The unique identifier of the process row in the 'process_data' table.
     * @param operationID The unique identifier for the current operation.
     * @param responseTime The response time for the specific process.
     * @param waitTime The waiting time for the specific process.
     * @param turnTime The turnaround time for the specific process.
     */
    public static void updateResult(int id, int operationID, int responseTime, int waitTime, int turnTime) {

        String sql = "UPDATE process_data SET responseTime = ?, waitingTime = ?, turnaroundTime = ? WHERE operationID = ? AND id = ?";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, responseTime);
            pstmt.setInt(2, waitTime);
            pstmt.setInt(3, turnTime);
            pstmt.setInt(4, operationID);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method is to retrieve the id associated with a specific operationID from the 'process_data' table.
     *
     * This method takes an operationID as a parameter and queries the 'process_data' table to retrieve
     * the ID of the corresponding row. The SQL query retrieves the ID with the lowest value (ORDER BY ASC LIMIT 1),
     * and the retrieved ID is returned.
     *
     * @param operationID The unique identifier for the operation.
     * @return The unique identifier (ID) associated with the specified operationID.
     */
    public static int retrieveID(int operationID) {
        int value = 0; // Initialize the variable to store the retrieved ID
        String sql = "SELECT id FROM process_data WHERE operationID = ? ORDER BY operationID ASC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, operationID);
            ResultSet rs = pstmt.executeQuery();

            // Check if there is a result before trying to retrieve data
            if (rs.next()) {
                value  = rs.getInt("id");
                System.out.println("ID is" + value ); //debug statement
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return value;
    }

    /**
     * This method is to retrieve an array of response times associated with a specific operationID from the 'process_data' table.
     *
     * This method queries the 'process_data' table to retrieve all response times corresponding to a specific
     * operationID. The response times are collected into an array, and this array is returned by the method.
     * The SQL query filters results based on the provided operationID.
     *
     * @return An array of response times associated with the specified operationID.
     */
    public static int [] getResTime(){
        String sql = "SELECT responseTime FROM process_data WHERE operationID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, data);
            ResultSet rs = pstmt.executeQuery();
            responseTimeList.clear();
            // Iterate through the result set and collect response times
            while (rs.next()) {
                int resTime = rs.getInt("responseTime");
                responseTimeList.add(resTime); // Add the response time to the list
                System.out.println("Retrieved responseTime: " + resTime); //debug statement
            }
            // Convert the list of response times to an array
            responseTime = responseTimeList.stream().mapToInt(Integer::intValue).toArray();

            return responseTime;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new int[0];
    }

    /**
     * This method is to process and retrieve response times for each process in the system.
     *
     * This method calls the getResTime method to retrieve an array of response times
     * associated with a specific operationID. It then prints each response time to the
     * console and constructs a formatted response string containing response times for
     * each process. The final response string is returned.
     *
     * @param numOfProcess The number of processes in the system.
     * @return A formatted string containing response times for each process.
     */
    public static String processResTime(int numOfProcess){
        int[] processResponse = getResTime(); // Retrieve an array of response times using the getResTime method
        for(int j = 0; j < numOfProcess; j++){
            System.out.println("ProcessResponse" + processResponse[j]);
        }
        String response = "";
        //Construct a formatted response string containing response times for each process
        for (int i = 0; i < numOfProcess; i++) {
            response += "Response Time for process " + (i + 1) + ": " + processResponse[i] + "\n";
        }
        return response;
    }

    /**
     * This method is to establish a connection to the SQLite database.
     *
     * This method creates a connection to the SQLite database using the specified URL.
     * If the connection is successful, it prints a confirmation message to the console.
     *
     * @return The established database connection.
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:C:/sqlite/telegrambot5.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

}