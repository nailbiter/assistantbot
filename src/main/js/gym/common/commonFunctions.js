function getProgram(){
	AddExercises([
		{
			obj: {
				name:"running",
				reps:"16 min",
			},
			index : 0,
		},
		{
			obj: {
				name:"back warmup",
				reps:"10k*15x, 15k*15x, 20k*15x, 25k*15x",
			},
			index : 1,
			filter:function(dayCount,weekCount){
				return (dayCount != 2);
			}
		},
	]);

	for(var i = 0; i < program.length; i++){
		for(var j = 0; j < program[i].program.length; j++){
			if(program[i].program[j].reps instanceof Array){
				program[i].program[j].reps = computeReps(program[i].program[j].reps,program[i].program[j].name,program[i].weekCount,program[i].dayCount)
			}
		}
	}
	return program;
}
function AddExercises(exercises){
	for(var index  = 0; index < exercises.length; index++){
		var exercise = exercises[index];
			for(var i = 0; i < program.length; i++){
				if( !('filter' in exercise) || exercise.filter(program[i].dayCount,program[i].weekCount))
					program[i].program.splice(exercise.index,0,exercise.obj);
			}
	}
}
function computeReps(reps,name,weekCount,dayCount){
	var max = 1.0;
	var res = "";
	if( name == 'bench' ) {
		max = getMax(BENCHWEIGHTS,weekCount);
	} else if( name == 'squat') {
		max = getMax(SQUATWEIGHTS,weekCount-2);
	}
	for(var i = 0; i < reps.length; i++){
		var rep = reps[i];
		res = res + (rep[0] + '(' + FloorUnit(max * rep[1],2.5)+'k)') + ', ';
	}
	return res;
}
function getMax(array,weekCount){
	for(var i = array.length -1 ;i >= 0; i--) {
		if(array[i][0] <= weekCount){
			return array[i][1]/100.0;
		}
	}
}
function FloorUnit(x,unit){
	return (Math.floor(x/unit))*unit;
}
