package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

//handle updates received from the Telegram server
public class simpleBot extends TelegramLongPollingBot {

    private static int numOfProcess; //store the total number of processes to be handled
    private static int currentProcessIndex = 0; //keep track of the current index of the process being entered

    private static String processID; //store the unique identifier of the process
    private static int burstTime; //store the burst time of the process
    private static int arrivalTime; //store the arrival time of the process
    private static int quantumNum; //store the quantum number for process scheduling
    @Override
    //get bot username
    public String getBotUsername() {
        return "Wen0331_Bot";
    }

    @Override
    //get bot token
    public String getBotToken() {
        return "6959837168:AAE4lYxIBO_W_tINGnu0HhVSxuF2d-fbOEo";
    }

    @Override
    //Handles incoming updates from the user in the Telegram chat
    public void onUpdateReceived(Update update) {
        // Check if the update contains a message from the user
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText(); // Get the text content of the user's message
            SendMessage response = new SendMessage(); //telegram response
            String chatId = update.getMessage().getChatId().toString(); // Get the unique identifier for the chat where the message was received
            //handle different user input
            switch(messageText){
                case "/start":
                case "/restart":
                    response.setChatId(chatId);
                    response.setText("Please enter the number of processes");
                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    finally {
                        System.out.println("The 'try catch' is finished.");
                    }
                    break;
                case "/terminate":
                    response.setChatId(chatId);
                    response.setText("Session Terminate\nThank you! ");
                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;
                default: // Handle user input for number of processes, quantum number, and process details
                    if (numOfProcess == 0) {
                        int num = Integer.parseInt(messageText);
                        if(num > 0){
                            try {
                                numOfProcess = Integer.parseInt(messageText);
                                response.setChatId(chatId);
                                response.setText("Number of process saved successfully.");
                                execute(response);
                                // Ask for process details
                                askForQuantumNum(chatId);
                            } catch (TelegramApiException | NumberFormatException e) {
                                response.setChatId(chatId);
                                response.setText("Invalid input. Please enter a valid number:");
                                try {
                                    execute(response);
                                } catch (TelegramApiException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else{
                            try {
                                response.setChatId(chatId);
                                response.setText("Invalid input. Please enter a valid number (more than 0).");
                                execute(response);
                            } catch (TelegramApiException | NumberFormatException e) {
                                response.setChatId(chatId);
                                response.setText("Invalid input. Please enter a valid number:");
                                try {
                                    execute(response);
                                } catch (TelegramApiException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    else if (quantumNum == 0) {
                        int num = Integer.parseInt(messageText);
                        if(num > 0) {
                            try {
                                quantumNum = Integer.parseInt(messageText);
                                Main.processInput(numOfProcess, quantumNum);
                                System.out.println("ccc");
                                response.setChatId(chatId);
                                response.setText("Quantum number saved successfully.");
                                execute(response);
                                // Ask for process details
                                askForProcessDetails(chatId);
                            } catch (TelegramApiException | NumberFormatException e) {
                                response.setChatId(chatId);
                                response.setText("Invalid input. Please enter a valid number:");
                                try {
                                    execute(response);
                                } catch (TelegramApiException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else{
                            try {
                                response.setChatId(chatId);
                                response.setText("Invalid input. Please enter a valid number (more than 0).");
                                execute(response);
                            } catch (TelegramApiException | NumberFormatException e) {
                                response.setChatId(chatId);
                                response.setText("Invalid input. Please enter a valid number:");
                                try {
                                    execute(response);
                                } catch (TelegramApiException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    else if(numOfProcess > 0){
                        boolean check = processUserInput(messageText, chatId);
                        if(check == true) {
                            // Handle process details and update the database
                            Main.handleInput(numOfProcess, processID, burstTime, arrivalTime);
                        }
                        else{
                            break;
                        }
                        // If all processes are entered, perform further processing
                        if (currentProcessIndex == numOfProcess) {

                            try{
                                Main.selectAll(numOfProcess, quantumNum);
                                String displayResponse = Main.processResTime(numOfProcess);
                                response.setChatId(chatId);
                                response.setText("All process details added successfully.\n" +
                                        "Calculating...\n\n" +
                                        displayResponse +
                                        "Average Response Time: " +
                                        Main.averageResponse +
                                        "\nAverage Waiting Time: " +
                                        Main.averageWait +
                                        "\nAverage Turnaround Time: " +
                                        Main.averageTurn +
                                        "\n\nThank you for using MySecondBot" +"!\n" +
                                        "Please type /restart\n to implement new processes or \n /terminate to end this session");
                                execute(response);
                                resetBotState(); // Reset for the next interaction
                            }catch(TelegramApiException e){
                                e.printStackTrace();
                            }
                        } else {
                            // Continue asking for process details
                            askForProcessDetails(chatId);
                        }
                    }


            }
        }
    }

    //Asks the user to input quantum number
    private void askForQuantumNum(String chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(chatId));
        response.setText("Please input quantum number");
        try {
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //Asks the user to input details for a specific process in the chat
    private void askForProcessDetails(String chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(chatId)); // Set the chat ID for the response
        response.setText("Please input the process ID, burst time, and arrival time for process "
                + (currentProcessIndex + 1) + " \n\n"
                + "Format: "
                + "ProcessID_BurstTime_ArrivalTime" + "\n"
                + "(Example: ABC_0_5)");
        try {
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //validate its format and processes user input in the format: ProcessID_BurstTime_ArrivalTime
    private boolean processUserInput(String userInput, String chatId) {
        boolean check = true;
        // Assuming userInput is in the format: ProcessID_BurstTime_ArrivalTime
        String[] parts = userInput.split("_");
        // Check if the input contains the underscore character or has the correct number of parts
        if (!userInput.contains("_") || parts.length != 3) {
            // Handle the case where the input format is incorrect
            SendMessage response = new SendMessage();
            response.setChatId(String.valueOf(chatId));
            response.setText("Invalid input format. Please use the format: ProcessID_BurstTime_ArrivalTime");
            try {
                check = false;
                execute(response);
                return check; // Exit the method without processing invalid input
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                // Assuming parts[0] is process ID, parts[1] is burst time, and parts[2] is arrival time
                processID = parts[0];
                burstTime= Integer.parseInt(parts[1]);
                arrivalTime = Integer.parseInt(parts[2]);

                currentProcessIndex++;
            } catch (NumberFormatException e) {
                // Handle the case where BurstTime or ArrivalTime is not a valid integer
                SendMessage response = new SendMessage();
                response.setChatId(String.valueOf(chatId));
                response.setText("Invalid input format. BurstTime and ArrivalTime must be valid integers.");
                try {
                    execute(response);
                    return check;
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return check;
    }

    //this method is to extract the bot name
    /*public static String extractBotName(String botToken) {
        // Check if the token is not null and has the expected format
        if (botToken != null && botToken.contains(":")) {
            // Split the token using colon as a delimiter
            String[] parts = botToken.split(":");
            // The first part should be the bot username
            return parts[0];
        }
        return null;  // Return null if the token format is not as expected
    }*/

    //Resets the state variables of the Telegram bot to their default values.
    //This method is typically called to clear any previously set values and prepare the bot for a new interaction or task.
    private void resetBotState() {
        numOfProcess = 0;
        quantumNum = 0;
        processID = "";
        burstTime = 0;
        arrivalTime = 0;
        currentProcessIndex = 0;
    }

}

