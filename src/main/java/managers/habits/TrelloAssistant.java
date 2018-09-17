package managers.habits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrelloAssistant {
	String key_, token_;
	HttpClient client_ = HttpClients.createDefault();
	TrelloAssistant(String key, String token) {
		key_ = key;
		token_ = token;
	}
	JSONArray getCardsInList(String listid) throws ClientProtocolException, IOException {
		System.out.println(String.format("id: %s", listid));
		String line = GetString(String.format("https://api.trello.com/1/lists/%s/cards?key=%s&token=%s&fields=name,due,dueComplete,id", listid,key_,token_),
				client_);
		JSONArray res = new JSONArray(line);
		System.out.println(String.format("res.len = %d", res.length()));
		return res;
	}
	void setCardDuedone(String cardid,boolean duedone) throws ClientProtocolException, IOException {
		HttpPut put = new HttpPut(String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s&dueComplete=%s", cardid,key_,token_,duedone?"true":"false"));
		client_.execute(put);
	}
	static String GetString(String url,HttpClient client_) throws ClientProtocolException, IOException {
		System.out.println(String.format("%s method for url: %s","get", url));
		HttpGet get = new HttpGet(url);
		System.out.println("here 1");
		HttpResponse chr = client_.execute(get);
		System.out.println("here 2");
		BufferedReader br = new BufferedReader(new InputStreamReader(chr.getEntity().getContent()));
		System.out.println("here 3");
		StringBuilder sb = new StringBuilder();
		String line;
		System.out.println("here 4");
		while ((line = br.readLine()) != null) {
			System.out.println("here in loop");
			sb.append(line);
	    }
		System.out.println(String.format("res: %s",sb.toString()));
		return sb.toString();
	}
	public void setLabel(String cardid, String labelColor) throws ClientProtocolException, IOException {
		System.out.println(String.format("cardid=%s, label=%s", cardid,labelColor));
		String uri = String.format("https://api.trello.com/1/cards/%s/labels?key=%s&token=%s&color=%s&name=failed", cardid,key_,token_,labelColor);
        PostString(uri,client_,true);
	}
	String findListByName(String boardId,String listName) throws Exception {
		String r = GetString(
				String.format("https://api.trello.com/1/boards/%s/lists?key=%s&token=%s&cards=none&fields=name", boardId,key_,token_),
				client_); 
		JSONArray res = new JSONArray(r);
		for(Object o:res) {
			JSONObject obj = (JSONObject)o;
			if(obj.getString("name").equals(listName))
				return obj.getString("id");
		}
		throw new Exception(String.format("no such list: %s", listName));
	}
	/*so far we support:
	 * 	name
	 * 	due
	 */
	void addCard(String idList,JSONObject card) throws ClientProtocolException, IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String uri = String.format("https://api.trello.com/1/cards?key=%s&token=%s&idList=%s&name=%s%s", 
				key_,
				token_,
				idList,
				URLEncoder.encode(card.getString("name")),
				card.has("due")?("&due="+URLEncoder.encode(dateFormat.format(((Date)card.get("due"))))):"");
		PostString(uri,client_,true);
	}
	static void PostString(String uri,HttpClient client_,boolean verbose) throws ClientProtocolException, IOException {
		System.out.println(String.format("uri: %s", uri));
		HttpPost put = new HttpPost(uri);
		if(!verbose) {
			client_.execute(put);
		}else{
			HttpResponse chr = client_.execute(put); 
			BufferedReader br = new BufferedReader(new InputStreamReader(chr.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
		    }
			System.out.println(String.format("reply: %s", sb.toString()));
		}
	}
}
