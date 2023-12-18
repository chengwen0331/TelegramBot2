package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class simpleBot extends TelegramLongPollingBot {

    private static int numOfProcess;
    private static int currentProcessIndex = 0;

    private static String processID;
    private static int burstTime;
    private static int arrivalTime;
    private static int quantumNum;
    @Override
    public String getBotUsername() {
        return "Wen0331_Bot";
    }

    @Override
    public String getBotToken() {
        return "6959837168:AAE4lYxIBO_W_tINGnu0HhVSxuF2d-fbOEo";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText(); //user input
            SendMessage response = new SendMessage(); //telegram response
            SendMessage response1 = new SendMessage(); //telegram response
            String chatId = update.getMessage().getChatId().toString();
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
                default:
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
                                        "Calculating averages...\n\n" +
                                        displayResponse +
                                        "Average Response Time: " +
                                        Main.averageResponse +
                                        "\nAverage Waiting Time: " +
                                        Main.averageWait +
                                        "\nAverage Turnaround Time: " +
                                        Main.averageTurn +
                                        "\n\nThank you for using MySecondBot!\n" +
                                        "Please type \n /restart to implement new processes or \n /terminate to end this session");
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

    private void askForProcessDetails(String chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(chatId));
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

    private boolean processUserInput(String userInput, String chatId) {
        boolean check = true;
        // Assuming userInput is in the format: ProcessID_BurstTime_ArrivalTime
        String[] parts = userInput.split("_");
        // Check if the input contains the underscore character
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

    private void resetBotState() {
        System.out.println("aabbcc");
        numOfProcess = 0;
        quantumNum = 0;
        processID = "";
        burstTime = 0;
        arrivalTime = 0;
        currentProcessIndex = 0;
    }

}

