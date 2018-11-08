package managers.misc;

import java.util.Date;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class NoteMaker {
	MongoCollection<Document> notesCollection_ = null;
	public NoteMaker(MongoClient mc) {
		notesCollection_ = mc.getDatabase("logistics").getCollection("notes");
	}
	public void makeNote(String noteContent) {
		Document doc = new Document();
		doc.put("date", new Date());
		doc.put("content", noteContent);
		notesCollection_.insertOne(doc);
	}
	
}
