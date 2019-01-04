package shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.jline.reader.Completer;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import managers.MyManager;
import util.KeyRing;
import util.UserCollection;
import util.Util;
import util.db.MongoUtil;
import util.parsers.StandardParserInterpreter;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import it.sauronsoftware.cron4j.Scheduler;
import static util.parsers.StandardParserInterpreter.CMD;

import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import static util.parsers.StandardParserInterpreter.Create;

public class InteractiveShell implements ResourceProvider,MyManager {
	private static final String ALEX = "alex";
	private static String PROMPT = "assistantbot> ";
	private String fileToOutputTo_;
	private StandardParserInterpreter parser_;
	private JSONObject userName_ = Util.GetDefaultUser();
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
		ArrayList<MyManager> managers = new ArrayList<MyManager>();
		managers.add(this);
		
		System.setProperty("DEBUG.MONGO", "false");
		System.setProperty("DB.TRACE", "false");
		KeyRing.init(profileObj.getString("NAME"),mc_);
		
		parser_ = Create(managers, profileObj.getJSONArray("MANAGERS"),this);
	}
	protected void start(JSONObject profileObj) throws Exception {
		ArrayList<String> commands = new ArrayList<String>(parser_.getDispatchTable().keySet());
		System.out.format("commands: %s\n", commands.toString());
		
		Completer completer = new StringsCompleter(commands);
        LineReader reader = LineReaderBuilder
        		.builder()
        		.completer(completer)
        		.history(new DefaultHistory())
        		.build();
        String line = null;
        
        while (true) {
            line = null;
            try {
                line = reader.readLine(PROMPT);
            	JSONObject res = parser_.parse(line);
            	sendMessage(parser_.getDispatchTable().get(res.getString(CMD)).getResultAndFormat(res));
            }
            catch(Exception e) {
            	sendMessage(String.format("%s: %s",e.getClass().getName() ,e.getMessage()));
            	e.printStackTrace();
            }
        }
	}
	@Override
	public int sendMessageWithKeyBoard(String msg, JSONArray categories) {
		TableBuilder tb = new TableBuilder();
		StringBuilder res = new StringBuilder();
		
		res.append("send keyboard:\n");
		
		tb.addNewlineAndTokens("#", "option");
		int i = 1;
		for(Object o:categories) {
			tb.newRow();
			tb.addToken(i++);
			tb.addToken((String)o);
		}
		res.append(tb.toString());
		
		sendMessage(res.toString());
		return 0;
	}
	@Override
	public MongoClient getMongoClient() {
		return mc_;
	}
	@Override
	public int sendMessage(String msg) {
//		msg = Util.CheckMessageLen(msg);
		
		System.out.println("\n"+msg);
		return -1;
	}
	@Override
	public Scheduler getScheduler() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int sendMessage(String string, MyManager testManager) throws Exception {
		//FIXME
		return sendMessage(string);
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
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	@Override
	public JSONObject getCommands() {
		return new JSONObject()
				.put("help", "display this message");
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has(CMD))
		{
			System.err.println(this.getClass().getName()+" got comd: "+res.getString(CMD));
			if(res.getString(CMD).compareTo("help")==0)
				return parser_.getHelpMessage();
		}
		throw new Exception(String.format("for res=%s", res.toString()));
	}
	@SuppressWarnings("deprecation")
	@Override
	public MongoCollection<Document> getCollection(UserCollection name) {
		return mc_.getDatabase(MongoUtil.getLogistics())
				.getCollection(String.format("%s.%s", 
						userName_.getString(Util.NAMEFIELDNAME) ,
						name.toString()));
	}
	@Override
	public JSONObject getUserObject() {
		return userName_;
	}
}
