package managers.tasks;

import java.util.ArrayList;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
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
	 * @param cards 
	 * @return
	 * @throws Exception
	 */
	protected ImmutablePair<Integer,Integer> getSegmentStartEnd(int segNum, JSONArray cards) throws Exception{
		if(segNum!=0 && segNum !=1)
			throw new Exception(String.format("segNum %d not supported", segNum));
		int separatorIndex = getSeparatorIndex(cards);
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
		return new ImmutablePair<Integer,Integer>(left,right);
	}
	public ArrayList<JSONObject> getCardsInSegment(int segNum) throws Exception {
		JSONArray cards = ta_.getCardsInList(LISTID);
		System.err.format("get cards for segment %d\n", segNum);
		ImmutablePair<Integer, Integer> bounds = 
				this.getSegmentStartEnd(segNum,cards);
		ArrayList<JSONObject> res = new ArrayList<JSONObject>();
		for(int i = bounds.left; i < bounds.right; i++ ) {
			JSONObject card = cards.getJSONObject(i);
			res.add(card);
			System.err.format("adding %s\n", card.toString(2));
		}
			
		return res;
	}
	protected int getSeparatorIndex(JSONArray cards) throws Exception{
		int res = IterableUtils.indexOf(cards, new Predicate<Object>() {
			@Override
			public boolean evaluate(Object arg0) {
				System.err.format("check: %s\n", arg0.toString());
				JSONObject obj = (JSONObject)arg0;
				return ((JSONObject)obj).getString("name").equals(SEPARATOR);
			}
		});
		return res;
	}
	public void moveTo(JSONObject card, String listid, int segNum) throws Exception {
		JSONArray cards = ta_.getCardsInList(LISTID);
		ImmutablePair<Integer, Integer> bounds = 
				this.getSegmentStartEnd(segNum,cards);
		System.err.format("preparing to move \"%s\" to seg #%d\n", 
				card.getString("name"),segNum);
		ta_.moveCard(
				card.getString("id"), 
				listid,
				(segNum==0)?"top":
					(bounds.left==cards.length())?"bottom":
						Integer.toString((int)((cards.getJSONObject(bounds.left-1).getInt("pos")+
								cards.getJSONObject(bounds.left).getInt("pos")+0.0)/2.0))
				);
	}
}
