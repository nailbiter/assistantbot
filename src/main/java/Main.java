import java.util.ArrayList;
import java.util.Map;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import assistantbot.MyAssistantBot;
import opts.Option;
import util.LocalUtil;

public class Main {
    public static void main(String[] args) {
    	ArrayList<Option> opts = new ArrayList<Option>();
    	opts.add(new Option('r',true));
    	//bot's name: also used to get token
    	opts.add(new Option('n',true));
    	//database password
    	opts.add(new Option('p',true));
    	Map<Character,Object> commandline = Option.processKeyArgs(Main.class.getName(), args, opts);
    	System.out.println(String.format("hi!: %s\nlen=%d", commandline.toString(),
    			((String)commandline.get('p')).length()));
    	LocalUtil.setJarFolder((String)commandline.get('r'));
//    	if(true) return;
    	
        ApiContextInitializer.init();

        System.out.println("here I go!");
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new MyAssistantBot(commandline));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}