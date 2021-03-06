package managers.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.collections4.Closure;
import org.json.JSONArray;
import org.json.JSONObject;

import static managers.habits.Constants.SEPARATOR;

import com.github.nailbiter.util.TrelloAssistant;

import util.AssistantBotException;
import util.TrelloUtil;

public class TrelloTaskList {
	private static final String SETSEGMENT = "SETSEGMENT";
	private static final String HASDUE = "HASDUE";
	private static final String ADDLABEL = "ADDLABEL";
	private static final Object HEAD = "HEAD";
	private String boardId_;
	private String listName_;
	private Integer segment_ = null;
	private TrelloAssistant ta_;
	private Closure<JSONObject> modifier_ = null;
	private Predicate<JSONObject> filter_ = null;
	private Integer head_ = null;;
	public TrelloTaskList(TrelloAssistant ta,String boardId, String listName) {
		ta_ = ta;
		boardId_ = boardId;
		listName_ = listName;
	}
	public TrelloTaskList setSegment(int segment) {
		segment_ = segment;
		return this;
	}
	public TrelloTaskList setModifier(Closure<JSONObject> clo) {
		modifier_  = clo;
		return this;
	}
	public TrelloTaskList setFilter(Predicate<JSONObject> filter) {
		filter_ = filter;
		return this;
	}
	public List<JSONObject> getTasks() throws Exception {
		List<JSONObject> res = new ArrayList<JSONObject>();
		if(segment_==null) {
			JSONArray tasks = ta_.getCardsInList(getListNamePrivate());
			for(Object o:tasks) {
				res.add((JSONObject) o);
			}
		} else {
			TrelloMover tm = new TrelloMover(ta_,getListNamePrivate(),SEPARATOR); 
			res = tm.getCardsInSegment(segment_);
		}
		
		if(head_ != null) {
			res = res.subList(0, head_);
		}
		if( filter_ != null ) {
			res.removeIf(filter_.negate());
		}
		if( modifier_ != null ) {
			for(JSONObject card:res) {
				modifier_.execute(card);
			}
		}
		
		return res;
	}
	public JSONObject addTask(JSONObject card) throws Exception {
		String li = getListNamePrivate();
		JSONObject res = ta_.addCard(li, card);
		if(segment_!=null) {
			new TrelloMover(ta_,li,SEPARATOR).moveTo(res,li,segment_);
		}
		return res;
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
	public static void Move(JSONObject card, TrelloTaskList trelloList, TrelloTaskList trelloList2) throws Exception {
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
	public TrelloTaskList modify(String type, Object data) {
		if(type.equals(SETSEGMENT)) {
			this.setSegment((Integer)data);
		} else if(type.equals(HASDUE)) {
			this.setFilter(new java.util.function.Predicate<JSONObject>() {
				@Override
				public boolean test(JSONObject object) {
					return TrelloUtil.HasDue(object);
				}
			});
		} else if(type.equals(ADDLABEL)) {
			this 				
			.setModifier(new Closure<JSONObject>() {
				@Override
				public void execute(JSONObject card) {
					card.getJSONArray("labels").put(new JSONObject().put("name", data));
				}
				}
			);
		} else if(type.equals(HEAD)) {
			setHead((Integer)data);
		}
		return this;
	}
	private void setHead(Integer size) {
		head_  = size;
	}
}
