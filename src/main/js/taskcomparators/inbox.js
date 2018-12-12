function main(cmdline){
	var split = cmdline.split(" ");
	return compare(JSON.parse(ScriptHelper.execute(split[0])),JSON.parse(ScriptHelper.execute(split[1])));
}
