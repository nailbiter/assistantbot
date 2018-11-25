package shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.MyManager;
import util.KeyRing;
import util.MongoUtil;
import util.Util;
import util.parsers.StandardParserInterpreter;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import it.sauronsoftware.cron4j.Scheduler;

import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class InteractiveShell implements ResourceProvider {
	private static String PROMPT = "assistantbot> ";
//	private static String HOME = "/Users/oleksiileontiev";
	private String fileToOutputTo_;
	static MongoClient mc_;
	public static void Start(JSONObject profileObj) throws Exception {
		(new InteractiveShell(profileObj)).start(profileObj);
	}
	protected InteractiveShell(JSONObject profileObj) throws Exception {
		boolean uselocaldb = profileObj.getBoolean("OFFLINE"); 
		System.out.format("USELOCALDB=%s\n", Boolean.toString(uselocaldb));
		Util.setProfileObj(profileObj.toString());
		DisableLogging();
		mc_ = uselocaldb ? new MongoClient() : MongoUtil.GetMongoClient( profileObj.getString("PASSWORD") );
		fileToOutputTo_ = Util.AddTerminalSlash(profileObj.getString("TMPFOLDER")) + profileObj.getString("FILETOSENDTO");
	}
	protected void start(JSONObject profileObj) throws Exception {
		ArrayList<MyManager> managers = new ArrayList<MyManager>();
		PopulateManagers(managers, profileObj,this);
		StandardParserInterpreter parser = 
				StandardParserInterpreter.Create(managers, profileObj.getJSONArray("managers"));
		parser.setPrefix("");
		
		ArrayList<String> commands = new ArrayList<String>();
		PopulateCommands(commands,managers);
		System.out.format("commands: %s\n", commands.toString());
		
		Completer completer = new StringsCompleter(commands);
        LineReader reader = LineReaderBuilder.builder().completer(completer).build();
        String line = null, str = null;
        
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
	@Override
	public int sendMessageWithKeyBoard(String msg, JSONArray categories) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public MongoClient getMongoClient() {
		return mc_;
	}
	@Override
	public int sendMessage(String msg) {
		System.out.println(msg);
		return -1;
	}
	@Override
	public Scheduler getScheduler() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int sendMessage(String string, MyManager testManager) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int sendMessageWithKeyBoard(String msg, List<List<InlineKeyboardButton>> makePerCatButtons) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int sendFile(String fn) throws IOException {
		File in = new File(fn);
		File out = new File(fileToOutputTo_);
		System.err.format("in=%s, out=%s\n", in.toString(),out.toString());
		Util.copyFileUsingStream(in, out);
		sendMessage(String.format("sent new file to %s", fileToOutputTo_));
		return 0;
	}
	private static void DisableLogging() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.OFF);
	}
	private static void PopulateCommands(ArrayList<String> commands, ArrayList<MyManager> managers) {
		for(MyManager am : managers) {
			JSONArray cmds = am.getCommands();
			for(Object o:cmds) {
				if(o instanceof JSONObject)
					commands.add(((JSONObject)o).getString("name"));
			}
		}
	}
	private static void PopulateManagers(ArrayList<MyManager> managers, JSONObject profileObj, ResourceProvider rp) throws Exception {
		System.setProperty("DEBUG.MONGO", "false");
		System.setProperty("DB.TRACE", "false");
		
		KeyRing.init(profileObj.getString("NAME"),mc_);

		Util.PopulateManagers(managers, profileObj.getJSONArray("MANAGERS"), rp);
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
