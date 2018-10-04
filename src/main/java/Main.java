import java.util.ArrayList;
import java.util.Map;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import assistantbot.MyAssistantBot;
import opts.Option;
import shell.InteractiveShell;
import util.LocalUtil;

public class Main {
    public static void main(String[] args) {
    	ArrayList<Option> opts = new ArrayList<Option>();
    	//res folder
    	opts.add(new Option('r',true));
    	//bot's name: also used to get token
    	opts.add(new Option('n',true));
    	//database password
    	opts.add(new Option('p',true));
    	//isOffline
    	opts.add(new Option('o',false));
    	Map<Character,Object> commandline = Option.processKeyArgs(Main.class.getName(), args, opts);
    	System.out.println(String.format("hi!: %s\nlen=%d", commandline.toString(),
    			((String)commandline.get('p')).length()));
    	LocalUtil.setJarFolder((String)commandline.get('r'));
    	
    	if(commandline.containsKey('o') && (boolean)commandline.get('o')) {
    		try {
				InteractiveShell.Start((String)commandline.get('p'));
			} catch (Exception e) {
				e.printStackTrace();
			}
    		System.exit(0);
    	}
    	
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