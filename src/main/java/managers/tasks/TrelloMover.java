package managers.tasks;

import java.util.ArrayList;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;

import managers.habits.Constants;

/**
 * 
 * @author oleksiileontiev
 * implement some additional functionality
 * based on {@link com.github.nailbiter.util.TrelloAssistant}.
 * In future this class may be integrated into 
 * {@link com.github.nailbiter.util.TrelloAssistant} 	
 */
public class TrelloMover {
	private TrelloAssistant ta_;
	private final String LISTID;
	private final String SEPARATOR;
	
	public TrelloMover(TrelloAssistant ta,String listid,String separator) {
		ta_ = ta;
		LISTID = listid;
		SEPARATOR = separator;
	}
	/**
	 * FIXME: support segNum>1
	 * @param segNum
	 * @return
	 * @throws Exception
	 */
	public ArrayList<JSONObject> getCardsInSegment(int segNum) throws Exception {
		if(segNum!=0 && segNum !=1)
			throw new Exception(String.format("segNum %d not supported", segNum));
		
		JSONArray cards = ta_.getCardsInList(LISTID);
		
		int separatorIndex = GetSeparatorIndex(cards);
		if( separatorIndex < 0 ) {
			if(segNum==0)
				separatorIndex = cards.length();
			else
				throw new Exception(String.format("no separator and segnum=%d", 
						segNum));
		}
		
		int left = 0,right = 0;
		if( segNum == 0 ) {
			left = 0;
			right = separatorIndex;
		} else if( segNum == 1 ) {
			left = separatorIndex + 1;
			right = cards.length();
		}
		
		ArrayList<JSONObject> res = new ArrayList<JSONObject>();
		for(int i = left; i < right; i++ )
			res.add(cards.getJSONObject(i));
		return res;
	}
	protected static int GetSeparatorIndex(JSONArray cards) throws Exception{
		int res = IterableUtils.indexOf(cards, new Predicate<Object>() {
			@Override
			public boolean evaluate(Object arg0) {
				System.err.format("check: %s\n", arg0.toString());
				JSONObject obj = (JSONObject)arg0;
				return ((JSONObject)obj).getString("name").equals(managers.habits.Constants.SEPARATOR);
			}
		});
//		if(res<0)
//			throw new Exception("no separator");
		return res;
	}
}
