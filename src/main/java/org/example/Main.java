package org.example;

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

    //Inserts numOfProcess and quantumNum into the 'processnum_data' table in the SQLite database
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

    //Insert the specified values into the 'process_data' table in the SQLite database
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

    //Retrieve the operationID from the 'processnum_data' table
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

    //Selects all process data from the 'process_data' table
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

    //Processes the retrieved data, performs scheduling, result calculation,and
    //updates the results in the database.
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

    //Handles the results of a scheduling operation and updates the database
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

    //Updates the result data for a specific process in the 'process_data' table
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

    //Retrieves id associated with a specific operationID from the 'process_data' table
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

    //Retrieves an array of response times associated with a specific operationID from the 'process_data' table
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

    //retrieves response times for each process
    //and constructs a formatted response string containing response times for each process.
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

    //Establishes a connection to the SQLite database
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