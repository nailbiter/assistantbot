function main(cmdline){
	var split = cmdline.split(" ");
	return compare(getVar(split[0]),getVar(split[1]));
}
