package shell;

import java.util.ArrayList;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;

import assistantbot.MyAssistantBot;
import managers.AbstractManager;
import managers.GermanManager;
import managers.MyManager;
import util.parsers.StandardParser;

public class InteractiveShell {
	private static final boolean USELOCALDB = false;
	private static String PROMPT = "assistantbot> ";
	public static void Start(String password) throws Exception {
		ArrayList<MyManager> managers = new ArrayList<MyManager>();
		PopulateManagers(managers,password);
		StandardParser parser = new StandardParser(managers);
		managers.add(parser);
		parser.setPrefix("");
		
		ArrayList<String> commands = new ArrayList<String>();
		PopulateCommands(commands,managers);
		System.out.format("commands: %s\n", commands.toString());
		
		Completer completer = new StringsCompleter(commands);
        LineReader reader = LineReaderBuilder.builder().completer(completer).build();
        String line = null,str = null;
        
        while (true) {
            line = null;
            try {
                line = reader.readLine(PROMPT);
                if(line.equals("exit")) {
                	return;
                }else {
                	JSONObject resp = parser.parse(line);
                	for(MyManager mm:managers) {
                		if((str = mm.getResultAndFormat(resp))!=null) {
                			System.out.format("%s\n", str);
                			break;
                		}
                	}
                }
            }
            catch(Exception e) {
            	e.printStackTrace();
            }
        }
	}
	private static void PopulateCommands(ArrayList<String> commands, ArrayList<MyManager> managers) {
		for(MyManager am : managers) {
			JSONArray cmds = am.getCommands();
			for(Object o:cmds) {
				if(o instanceof JSONObject)
					commands.add(((JSONObject)o).getString("name"));
			}
		}
		commands.add("exit");
	}
	private static void PopulateManagers(ArrayList<MyManager> managers, String password) {
		MongoClient mc = USELOCALDB ? new MongoClient() : MyAssistantBot.GetMongoClient(password);
		
		System.setProperty("DEBUG.MONGO", "false");
		System.setProperty("DB.TRACE", "false");

		managers.add(new GermanManager(mc));
		managers.add(new MyManager() {
			@Override
			public String processReply(int messageID, String msg) {
				return null;
			}
			@Override
			public String getResultAndFormat(JSONObject res) throws Exception {
				return null;
			}
			@Override
			public JSONArray getCommands() {
				return new JSONArray().put("cmd");
			}
		});
	}
}
