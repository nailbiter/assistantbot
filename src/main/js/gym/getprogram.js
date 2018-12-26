function main(cmdline){
	var split = cmdline.split(" ");
	var weekCount = split[0]*1.0,
		dayCount = split[1]*1.0;
	var program = getProgram();
	for(var i = 0; i < program.length; i++) {
		if( program[i].dayCount == dayCount && program[i].weekCount == weekCount ) {
			return JSON.stringify(program[i].program);
		}
	}
}
