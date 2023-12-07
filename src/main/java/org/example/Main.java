package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static int numOfProcess;
    static int quantumNum;
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
    static int data;
    static List<String> processIDList = new ArrayList<>();
    static List<Integer> burstTimeList = new ArrayList<>();
    static List<Integer> arrivalTimeList = new ArrayList<>();
    static Connection conn = connect();
    public static void main(String[] args) throws TelegramApiException {

        connect();
        CreateTable generate = new CreateTable();
        generate.createNewTable();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new simpleBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
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

    public static void processInput(int numOfProcess, int quantumNum) {

        String sql = "INSERT INTO processnum_data(numOfProcess, quantumNum) VALUES(?,?)";
        try{
            System.out.println("bbb");
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, numOfProcess);
            pstmt.setInt(2, quantumNum);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void handleInput(int numOfProcess, String processID, int burstTime, int arrivalTime) {
        data = selectData(numOfProcess);
        //String sql = "INSERT INTO process_data(operationID, numOfProcess, processID, burstTime, arrivalTime) VALUES(?,?,?,?,?)";
        String sql = "INSERT INTO process_data(operationID, processID, burstTime, arrivalTime) VALUES(?,?,?,?)";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, data);
            //pstmt.setInt(2, numOfProcess);
            pstmt.setString(2, processID);
            pstmt.setInt(3, burstTime);
            pstmt.setInt(4, arrivalTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int selectData(int number) {
        int data = 0;
        //String sql = "SELECT operationID FROM processnum_data WHERE numOfProcess = ?";
        String sql = "SELECT operationID FROM processnum_data WHERE numOfProcess = ? ORDER BY operationID DESC LIMIT 1";
//error occur here
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            //pstmt.setInt(1, count);  // Set the value for the placeholder
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

    public static void selectAll(int num, int quantum){
        //String sql = "SELECT * FROM process_data WHERE numOfProcess = ? AND operationID = ?";
        String sql = "SELECT * FROM process_data WHERE operationID = ?";
        processID= new String[num];
        burstTime= new int[num];
        arrivalTime= new int[num];
        System.out.print("Data is " + data);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                //pstmt.setInt(1, num);  // Set the value for the placeholder
                pstmt.setInt(1, data);
                ResultSet rs = pstmt.executeQuery();
                processIDList.clear();
                burstTimeList.clear();
                arrivalTimeList.clear();

                while (rs.next()) {
                    operationID = rs.getInt("operationID");
                    System.out.println("Operation ID are" + operationID);
                    //numOfProcess = rs.getInt("numOfProcess");
                    //quantumNum = rs.getInt("quantumNum");
                    String currentProcessID = rs.getString("processID");
                    processIDList.add(currentProcessID);
                    int currentBurstTime = rs.getInt("burstTime");
                    burstTimeList.add(currentBurstTime);
                    int currentArrivalTime = rs.getInt("arrivalTime");
                    arrivalTimeList.add(currentArrivalTime);
                //scheduler.setNumOfProcess(rs.getInt("numOfProcess"));
                //scheduler.setQuantumNum(rs.getInt("quantumNum"));
                //scheduler.setBurstTime(rs.getInt("process_data.burstTime"));
                }
                processID = processIDList.toArray(new String[0]);
                burstTime = burstTimeList.stream().mapToInt(Integer::intValue).toArray();
                arrivalTime = arrivalTimeList.stream().mapToInt(Integer::intValue).toArray();
                processRetrievedData(operationID, num, quantum, processID, burstTime, arrivalTime);
            } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void processRetrievedData(int operationID, int numOfProcess, int quantumNum, String [] processID, int [] burstTime, int [] arrivalTime) {
        // Process or store the retrieved data, e.g., in another class (Scheduler)
        int[] tem_burstTime = new int[numOfProcess];
        //tem_burstTime = burstTime;
        for(int i = 0; i < numOfProcess; i++){
            tem_burstTime[i] = burstTime[i];
            System.out.println("B is" + burstTime[i]);
            System.out.println("Arrival is" + arrivalTime[i]);
            //System.out.println("Burst Time is " + tem_burstTime[i]);
        }
        Scheduler scheduler = new Scheduler(numOfProcess, quantumNum, processID, burstTime, arrivalTime);
        scheduler.runScheduler();
        Result resultCalculator = new Result(numOfProcess, tem_burstTime, arrivalTime, scheduler.getTurnaroundTime(), scheduler.getStartTime());
        resultCalculator.calculateResults();
        resultCalculator.displayResults();
        averageWait = resultCalculator.getAvgWaitTime();
        averageTurn = resultCalculator.getAvgTurnTime();
        averageResponse = resultCalculator.getAvgResTime();
        waitTime = resultCalculator.getWaitTime();
        turnTime = resultCalculator.getTurnTime();
        responseTime = resultCalculator.getResponseTime();
        handleResult(operationID, averageWait, averageTurn, averageResponse);
        for(int i = 0; i < numOfProcess; i++){
            int response = responseTime[i];
            int wait = waitTime[i];
            int turn = turnTime[i];
            updateResult(operationID, response, wait, turn);
        }
    }

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

    public static void updateResult(int operationID, int responseTime, int waitTime, int turnTime) {
        String sql = "UPDATE process_data SET responseTime = ?, waitingTime = ?, turnaroundTime = ? WHERE operationID = ?";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, responseTime);
            pstmt.setInt(2, waitTime);
            pstmt.setInt(3, turnTime);
            pstmt.setInt(4, operationID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

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
        } /*finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection to SQLite has been closed.");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }*/
        return conn;
    }


}