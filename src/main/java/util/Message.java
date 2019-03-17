package util;

public class Message {
	private String msg_;
	private boolean isHtml_ = false;
	public Message(String msg) {
		msg_ = msg;
	}
	public String getMessage() {
		return msg_;
	}
	public Message setHtml() {
		isHtml_ = true;
		return this;
	}
	public boolean isHtml() {
		return isHtml_;
	}
}
