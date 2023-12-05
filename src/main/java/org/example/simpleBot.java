package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class simpleBot extends TelegramLongPollingBot {

    private int numOfProcess = 0;
    private int currentProcessIndex = 0;
    private String[] processID;
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
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                SendMessage response = new SendMessage();
                if (messageText.equals("/processnum")) {
                    // Ask the user for input
                    String message = "Please enter the number of process";
                    //SendMessage response = new SendMessage();
                    response.setChatId(update.getMessage().getChatId().toString());
                    response.setText(message);
                    try {
                        execute(response);
                        String userInput = update.getMessage().getText();
                        numOfProcess = Integer.parseInt(userInput);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                }
                else if (messageText.equals("/displaynum")) {
                    response.setChatId(update.getMessage().getChatId().toString());
                    String message = Integer.toString(numOfProcess);
                    response.setText("The number of process is " + message);
                    //sendMessage(update.getMessage().getChatId(), "Please input the process ID for process " + (currentProcessIndex + 1) + ":");

                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                else if (messageText.equals("/getprocessid")) {
                    if (numOfProcess > 0 && currentProcessIndex < numOfProcess) {
                        // Ask the user for the process ID for the current process index
                        response.setChatId(update.getMessage().getChatId().toString());
                        response.setText("Please input the process ID for process " + (currentProcessIndex + 1) + ":");
                        //sendMessage(update.getMessage().getChatId(), "Please input the process ID for process " + (currentProcessIndex + 1) + ":");
                    } else {
                        // If the user hasn't provided the number of processes, ask them to use /getnum
                        response.setChatId(update.getMessage().getChatId().toString());
                        response.setText("Please select /processnum to specify the number of processes first.");
                        //sendMessage(update.getMessage().getChatId(), "Please use /getnum to specify the number of processes first.");
                    }
                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                else if (messageText.equals("/terminate")) {
                    response.setChatId(update.getMessage().getChatId().toString());
                    response.setText("Session Terminate\nThank you! ");
                    //sendMessage(update.getMessage().getChatId(), "Please input the process ID for process " + (currentProcessIndex + 1) + ":");

                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // Assuming this is where you collect and store the process ID
                    if (currentProcessIndex < numOfProcess) {
                        // Assuming the user's response is the process ID
                        processID[currentProcessIndex] = messageText;
                        currentProcessIndex++;

                        // If all process IDs are collected, proceed to the next step
                        if (currentProcessIndex == numOfProcess) {
                            // Call the method in the Main class to perform further processing
                            Main.processInput(numOfProcess, processID);

                            // Reset the process index for the next interaction
                            currentProcessIndex = 0;
                        } else {
                            // If there are more processes to collect, ask for the next one
                            response.setChatId(update.getMessage().getChatId().toString());
                            response.setText("Please input the process ID for process " + (currentProcessIndex + 1) + ":");
                        }
                    }
                }
            }
    }

}

