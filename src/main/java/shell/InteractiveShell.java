package shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

import assistantbot.BasicUserData;
import assistantbot.ResourceProvider;
import managers.MyManager;
import util.KeyRing;
import util.Message;
import util.UserCollection;
import util.Util;
import util.db.MongoUtil;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import it.sauronsoftware.cron4j.Scheduler;
import static util.parsers.StandardParserInterpreter.CMD;

import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import static util.parsers.StandardParserInterpreter.Create;

public class InteractiveShell extends BasicUserData implements ResourceProvider, MyManager {
	private static String PROMPT = "assistantbot> ";
	private String fileToOutputTo_;
	private ImmutablePair<Map<String, Object>, Transformer<Object, Message>> lastKeyboard_;
	private int messageId_;
	static MongoClient mc_;
	public static void Start(JSONObject profileObj) throws Exception {
		(new InteractiveShell(profileObj)).startMe(profileObj);
	}
	protected InteractiveShell(JSONObject profileObj) throws Exception {
		super(true);
		messageId_ = 0;
		boolean uselocaldb = profileObj.getBoolean("OFFLINE"); 
		System.out.format("USELOCALDB=%s\n", Boolean.toString(uselocaldb));
		Util.setProfileObj(profileObj.toString());
		DisableLogging();
		mc_ = uselocaldb ? new MongoClient() : MongoUtil.GetMongoClient( profileObj.getString("PASSWORD") );
		fileToOutputTo_ = Util.AddTerminalSlash(profileObj.getString("TMPFOLDER")) + profileObj.getString("FILETOSENDTO");
		managers_.add(this);
		
		System.setProperty("DEBUG.MONGO", "false");
		System.setProperty("DB.TRACE", "false");
		KeyRing.init(profileObj.getString("NAME"),mc_);
		
		parser_ = Create(managers_, profileObj.getJSONArray("MANAGERS"),this);
		scheduler_.start();
	}
	protected void startMe(JSONObject profileObj) throws Exception {
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
            	sendMessage(new Message(String.format("%s: %s",e.getClass().getName() ,e.getMessage())));
            	e.printStackTrace();
            }
        }
	}
	@Override
	public int sendMessageWithKeyBoard(Message msg, JSONArray categories) {
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
		
		sendMessage(new Message(res.toString()));
		return 0;
	}
	@Override
	public MongoClient getMongoClient() {
		return mc_;
	}
	@Override
	public int sendMessage(Message msg) {
//		msg = Util.CheckMessageLen(msg);
		
		System.out.format("%03d: %s\n",messageId_,msg.getMessage());
		return messageId_++;
	}
//	@Override
//	public int sendMessageWithKeyBoard(Message msg, List<List<InlineKeyboardButton>> makePerCatButtons) {
//		return 0;
//	}
	@Override
	public int sendFile(String fn) throws IOException {
		File in = new File(fn);
		File out = new File(fileToOutputTo_);
		System.err.format("in=%s, out=%s\n", in.toString(),out.toString());
		Util.copyFileUsingStream(in, out);
		sendMessage(new Message(String.format("sent new file to %s", fileToOutputTo_)));
		return 0;
	}
	private static void DisableLogging() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.OFF);
	}
	@SuppressWarnings("deprecation")
	@Override
	public MongoCollection<Document> getCollection(UserCollection name) {
		return mc_.getDatabase(MongoUtil.getLogistics())
				.getCollection(String.format("%s.%s", 
						userObject_.getString(Util.NAMEFIELDNAME) ,
						name.toString()));
	}
	@Override
	public int sendMessageWithKeyBoard(Message msg, Map<String, Object> map, Transformer<Object,Message> me) {
		lastKeyboard_ = new ImmutablePair<Map<String, Object>, Transformer<Object,Message>>(map,me);
		TableBuilder tb = new TableBuilder();
		int i = 0;
		for(String s:map.keySet().toArray(new String[] {})) {
			tb.addTokens(Integer.toString(i++),s);
		}
		sendMessage(new Message(tb.toString()));
		return 0;
	}
	public Message msgreply(JSONObject arg) {
		return processReply(arg.getInt("msgid"), arg.getString("msgcontent"));
	}
	public Message keyboard(JSONObject arg) {
		int num = arg.getInt("num");
		String[] keys = lastKeyboard_.left.keySet().toArray(new String[] {});
		return lastKeyboard_.right.transform(lastKeyboard_.left.get(keys[num]));
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
