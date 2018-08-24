package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class LocalUtil {
	protected static boolean isInit = false;
//	protected static boolean onMac = false;
	public static String jarFolder; 
	protected static void init() throws Exception
	{
		if(!isInit)
		{
			isInit = true;
		}
	}
	public static String getJarFolder() {return jarFolder;}
	public static JSONArray getJSONArrayFromRes(Object me,String name) throws Exception
	{
		init();
		String fname = jarFolder+name+".json";
		System.out.println(String.format("%s is trying to get %s",LocalUtil.class.getName(),fname));
        String theString = FileToString(fname);
        System.out.println("theString="+theString);
        return (JSONArray)(new JSONTokener(theString)).nextValue();
	}
	static String FileToString(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		// delete the last new line separator
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		reader.close();

		String content = stringBuilder.toString();
		return content;
	}
}