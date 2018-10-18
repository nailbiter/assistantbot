import java.util.ArrayList;
import java.util.Map;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import assistantbot.MyAssistantBot;
import opts.Option;
import shell.InteractiveShell;
import util.LocalUtil;
import static opts.Option.ArgEnum;

public class Main {
    public static void main(String[] args) {
    	ArrayList<Option> opts = new ArrayList<Option>();
    	opts.add(new Option('r',ArgEnum.HASARGUMENT,"res folder"));
    	opts.add(new Option('n',ArgEnum.HASARGUMENT,"bot's name: also used to get token"));
    	opts.add(new Option('p',ArgEnum.HASARGUMENT,"database password"));
    	opts.add(new Option('t',ArgEnum.HASARGUMENT,"reboot file"));
    	opts.add(new Option('o',ArgEnum.HASARGUMENT,String.format("isOffline, %s=local|remote", Option.DEFARGNAME)));
    	
    	Map<Character,Object> commandline = Option.processKeyArgs(Main.class.getName(), args, opts);
    	System.out.println(String.format("hi!: %s\nlen=%d", commandline.toString(),
    			((String)commandline.get('p')).length()));
    	
    	LocalUtil.SetJarFolder((String)commandline.get('r'));
    	LocalUtil.SetRebootFileName((String)commandline.get('t'));
    	
    	if(commandline.containsKey('o')) {
    		try {
				InteractiveShell.Start((String)commandline.get('p'),(String)commandline.get('o'));
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
    	} else {
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
}