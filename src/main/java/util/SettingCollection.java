package util;

public enum SettingCollection {
	USERS ("users")
	,PARAMS("params")
	,KEYRING("keyring")
	;
	
	private final String name_;
	private SettingCollection(String name) {
		name_ = name;
	}
	public String toString() {
		return "_"+name_;
	}
}
