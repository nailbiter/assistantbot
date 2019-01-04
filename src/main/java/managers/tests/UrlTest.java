package managers.tests;

import java.util.ArrayList;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import util.UserCollection;
import util.db.MongoUtil;

public class UrlTest extends JsonTest {
	public UrlTest(JSONObject obj) {
		obj_ = obj;
	}

	public static void AddTests(final ArrayList<JsonTest> testContainer, ResourceProvider rp) throws Exception
	{
		MongoCollection<Document> tests = 
//				rp.getDatabase(MongoUtil.LOGISTICS).getCollection("urlTests");
				rp.getCollection(UserCollection.URLTESTS);
		tests.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				JSONObject obj = new JSONObject(arg0.toJson());
				testContainer.add(new UrlTest(obj));
			}
		});
	}
	@Override
	public String[] isCalled() {
		return new String[] {obj_.getString("url")};
	}
	@Override
	public String processReply(String msg) {
		return null;
	}
	@Override
	public String showTest() {
		return obj_.getString("url");
	}
}
