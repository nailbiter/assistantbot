import java.util.ArrayList;
import java.util.Map;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import opts.Option;
import util.LocalUtil;

public class Main {
    public static void main(String[] args) {
    	ArrayList<Option> opts = new ArrayList<Option>();
    	opts.add(new Option('r',true));
    	opts.add(new Option('t',true));
    	opts.add(new Option('n',true));
    	Map<Character,Object> commandline = Option.processKeyArgs(Main.class.getName(), args, opts);
    	System.out.println(String.format("commandline: %s", commandline.toString()));
//    	if(true) return;

        ApiContextInitializer.init();
        util.LocalUtil.jarFolder = (String)commandline.get('r');

        System.out.println("here I go!");
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new MyAssistantBot(commandline));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}