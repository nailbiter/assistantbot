package util;

public enum UserCollection {
	URLTESTS ("urlTests")
	,TIMECATS ("timecats")
	,TIME ("time")
	,TASKLOG ("taskLog")
	,SLEEPINGTIMES ("sleepingtimes")
	,SCORESOFTESTS ("scoresOfTests")
	,REPORTDESCRIPTIONS ("reportDescriptions")
	,POSTPONEDTASKS ("postponedTasks")
	,PARADIGMTESTS ("paradigmTests")
	,NOTES("notes")
	,MONEYCATS("moneycats")
	,MONEY("money")
	,HABITSPUNCH("habitspunch")
	,HABITS("habits")
	,GYMPROGRAM("gymProgram")
	,GYMLOG("gymLog")
	,GENDER("gender")
	,PARAMS("params")
	;
	
	private final String name_;
	private UserCollection(String name) {
		name_ = name;
	}
	public String toString() {
		return name_;
	}
}
