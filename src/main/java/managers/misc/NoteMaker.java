package managers.misc;

import java.util.Date;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import util.UserCollection;

public class NoteMaker {
	MongoCollection<Document> notesCollection_ = null;
	public NoteMaker(ResourceProvider rp) {
		notesCollection_ = rp.getCollection(UserCollection.NOTES);
	}
	public void makeNote(String noteContent) {
		Document doc = new Document();
		doc.put("date", new Date());
		doc.put("content", noteContent);
		notesCollection_.insertOne(doc);
	}
	
}
