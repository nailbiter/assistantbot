/*function main(cmdline){
	var split = cmdline.split(" ");
	return compare(getVar(split[0]),getVar(split[1]));
}*/

function compare(s1,s2) {
	compareObjects(JSON.parse(s1),JSON.parse(s2));
}
