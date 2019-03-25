package managers.tasks;

import java.util.ArrayList;

import org.json.JSONObject;
import static managers.habits.Constants.SEPARATOR;

import com.github.nailbiter.util.TrelloAssistant;

import util.AssistantBotException;

public class TrelloList {
	private String boardId_;
	private String listName_;
	private Integer segment_ = null;
	private TrelloAssistant ta_;
	public TrelloList(TrelloAssistant ta,String boardId, String listName) {
		ta_ = ta;
		boardId_ = boardId;
		listName_ = listName;
	}
	public TrelloList setSegment(int segment) {
		segment_ = segment;
		return this;
	}
	public ArrayList<JSONObject> getTasks() throws Exception {
		if(segment_==null) {
			ta_.getCardsInList(getListNamePrivate());
			return null;
		} else {
			TrelloMover tm = new TrelloMover(ta_,getListNamePrivate(),SEPARATOR); 
			return tm.getCardsInSegment(segment_);
		}
	}
	public JSONObject addTask(JSONObject card) throws Exception {
		String li = getListNamePrivate();
		JSONObject res = ta_.addCard(li, card);
		if(segment_!=null) {
			new TrelloMover(ta_,li,SEPARATOR).moveTo(res,li,segment_);
		}
		return null;
	}
	private String getListNamePrivate() throws Exception {
		return ta_.findListByName(boardId_,listName_);
	}
	/**
	 * @deprecated
	 * @return
	 * @throws Exception 
	 */
	public String getListName() throws Exception {
		return getListNamePrivate();
	}
	public static void Move(JSONObject card, TrelloList trelloList, TrelloList trelloList2) throws Exception {
		// TODO Auto-generated method stub
		if(trelloList.segment_!=null && trelloList2.segment_!=null) {
			new TrelloMover(trelloList.ta_,trelloList.getListNamePrivate(),SEPARATOR)
			.moveTo(card,trelloList2.getListNamePrivate(),trelloList2.segment_);
		} else {
			throw new AssistantBotException(AssistantBotException.Type.TRELLOLIST
					,String.format("Move %s %s %s", card.toString(2)
							,trelloList.getListNamePrivate()
							,trelloList2.getListNamePrivate()));
		}
	}
}
