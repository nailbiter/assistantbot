package managers.habits;

public class Constants {
	public static enum BOARDIDS {
		DREAMPIRATES("nqI8xwIu")
		,HABITS("kDCITi9O")
		;
		private String name_;
		BOARDIDS(String name){
			name_ = name;
		}
		public String toString() {
			return name_;
		}
	}
	public static enum LISTNAMES {
		todo("todo")
		,TODOcode("TODO: code")
		;
		private String name_;
		LISTNAMES(String name){
			name_ = name;
		}
		public String toString() {
			return name_;
		}
	}
	/**
	 * @deprecated move to BOARDIDS
	 */
	public static final String HABITBOARDID = "kDCITi9O";
	public static final String PENDINGLISTNAME = "PENDING";
	public static final String TODOLISTNAME = "TODO";
	public static final String FAILLABELCOLOR = "green";
	/**
	 * @deprecated move to BOARDIDS
	 */
	public static final String INBOXBOARDID = "foFETfOx";
	public static final String INBOXLISTNAME = "inbox";
	public static final String SEPARATOR = "------------------------------";
	public static final String INBOXBOARDIDLONG = "5a83f33d7c047209445249dd";
}
