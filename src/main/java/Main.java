import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import assistantbot.MyAssistantBot;

public class Main {
    public static void main(String[] args) {

    	System.out.println("hi!");
    	if(true) return;
        ApiContextInitializer.init();

        System.out.println("here I go!");
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new MyAssistantBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}