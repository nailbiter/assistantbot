var BENCHWEIGHTS = [
	[1,3,185],
	[4,6,185+5],
	[7,9,185+10],
	[10,12,185+15],
];
function getProgram(){
	AddRunning(program);
	for(var i = 0; i < program.length; i++){
		for(var j = 0; j < program[i].program.length; j++){
			if(program[i].program[j].reps instanceof Array){
				program[i].program[j].reps = computeReps(program[i].program[j].reps,program[i].program[j].name,program[i].weekCount,program[i].dayCount)
			}
		}
	}
	return program;
}
function AddRunning(program){
	var runObj = {
		name:"running",
		reps:"16 min",
	};
	for(var i = 0; i < program.length; i++){
		program[i].program.splice(0,0,runObj);
	}
}
function computeReps(reps,name,weekCount,dayCount){
	var max = 1.0;
	var res = "";
	if( name == 'bench' ) {
		for(var i = 0; i < BENCHWEIGHTS.length; i++){
			if(BENCHWEIGHTS[i][0] <= weekCount && weekCount <= BENCHWEIGHTS[i][1]){
				max = BENCHWEIGHTS[i][2]/100.0;
			}
		}
	}
	for(var i = 0; i < reps.length; i++){
		var rep = reps[i];
		res = res + (rep[0] + '(' + FloorUnit(max * rep[1],2.5)+')') + ', ';
	}
	return res;
}
function TwoSignsAfterComma(x){
	return (Math.floor(100.0*x))/100.0;
}
function FloorUnit(x,unit){
	return (Math.floor(x/unit))*unit;
}
