package managers.mongomanager;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import util.db.MongoUtil;

public class MongoManagerHelper {

	public static final String IDFIELD = "_id";

	public static int Fixdate(final String collname,final String key, MongoDatabase db) throws Exception {
		final ArrayList<ImmutablePair<ObjectId, String>> arr = 
				new ArrayList<ImmutablePair<ObjectId,String>>();
		MongoCollection<Document> coll = db.getCollection(collname);
		coll.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				if( arg0.containsKey(key) ) {
					String res = null;
					try {
						res = arg0.getString(key);
					} catch(ClassCastException e) {
					}
					if( res != null )
						arr
						.add(
								new ImmutablePair<ObjectId,String>(
										arg0.getObjectId(IDFIELD), 
										arg0.getString(key))
								);
				}
			}
		});
		for(ImmutablePair<ObjectId, String> tuple:arr) {
			Date d = MongoUtil.MongoDateStringToLocalDate(tuple.right);
			coll.updateOne(Filters.eq(IDFIELD, tuple.left), Updates.set(key,d));
		}
		return arr.size();
	}

	public static int Fixint(final String collname, final String key, MongoDatabase db) {
		final ArrayList<ImmutablePair<ObjectId, Integer>> arr = 
				new ArrayList<ImmutablePair<ObjectId,Integer>>();
		MongoCollection<Document> coll = db.getCollection(collname);
		coll.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				if( arg0.containsKey(key) ) {
					Integer res = null;
					try {
						res = arg0.getInteger(key);
					} catch(ClassCastException e) {
					}
					if( res != null )
						arr
						.add(
								new ImmutablePair<ObjectId,Integer>(
										arg0.getObjectId(IDFIELD), 
										res)
								);
				}
			}
		});
		for(ImmutablePair<ObjectId, Integer> tuple:arr) {
			Double d = 1.0*tuple.right; 
			coll.updateOne(Filters.eq(IDFIELD, tuple.left), Updates.set(key,d));
		}
		return arr.size();
	}

}
