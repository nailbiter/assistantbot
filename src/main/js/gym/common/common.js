var BENCHWEIGHTS = [
	[1,3,185],
	[4,6,185+5],
	[7,9,185+10],
	[10,12,185+15],
];
var program = [
	{
		"weekCount":1,
		"dayCount":1,
		"program":[
			{
				"name":"bench",
				"reps":"8*3*62.5(115), 8*67.5(125)"
			},
			{
				"name":"fly",
				"reps":"3*10"
			},
			{
				"name":"stand press",
				"reps":"3*6"
			}
		]
	},
	{
		"weekCount":1,
		"dayCount":2,
		"program":[
			{
				"name":"brus",
				"reps":"2*12, 3*25*8"
			},
			{
				"name":"razg",
				"reps":"2*15, 3*10"
			},
			{
				"name":"bic",
				"reps":"2*15, 3*10"
			},
			{
				"name":"molot",
				"reps":"2*15*15, 3*12*22.5"
			}
		]
	},
	{
		"weekCount":1,
		"dayCount":3,
		"program":[
			{
				"name":"tricPull",
				"reps":"5*10*6plates"
			},
			{
				"name":"grud",
				"reps":"3x5x27.5"
			},
			{
				"name":"squat",
				"reps":"72*10/5 (155)"
			},
			{
				"name":"stan",
				"reps":"3x8x50"
			},
			{
				"name":"matr",
				"reps":"?"
			},
			{
				"name":"pullup",
				"reps":"2xMax"
			}
		]
	},
	{
		"weekCount":1,
		"dayCount":4,
		"program":[
			{
				"name":"bench",
				"reps":"5(115), 5(1*)(130), 5/5(137.5)"
			},
			{
				"name":"fly",
				"reps":"3*10"
			},
			{
				"name":"stand press",
				"reps":"3*6"
			},
		]
	},
	{
		"weekCount":2,
		"dayCount":1,
		"program":[
			{
				"name":"bench",
				"reps":"5*62.5(115), 5(1)*70(130),5*3*77.5(142.5)"
			},
			{
				"name":"fly",
				"reps":"3*10-12 (??)"
			},
			{
				"name":"stand press",
				"reps":"3*6"
			}
		]
	},
	{
		"weekCount":2,
		"dayCount":2,
		"program":[
			{
				"name":"running",
				"reps":"16 min"
			},
			{
				"name":"rsid",
				"reps":"2*15, 3*10"
			},
			{
				"name":"bic",
				"reps":"2*15, 3*10"
			},
			{
				"name":"molot",
				"reps":"2*15*15, 3*12*22.5"
			}
		]
	},
	{
		"weekCount":2,
		"dayCount":3,
		"program":[
			{
				"name":"running",
				"reps":"16 min"
			},
			{
				"name":"tricPull",
				"reps":"4*10x*35k, 1*8x*40k"
			},
			{
				"name":"grud",
				"reps":"3x5x27.5"
			},
			{
				"name":"squat",
				"reps":"72*10/5 (155)"
			},
			{
				"name":"stan",
				"reps":"3x8x50"
			},
			{
				"name":"matr",
				"reps":"28.5k*12x*3"
			},
			{
				"name":"pullup",
				"reps":"2xMax"
			}
		]
	},
	{
		weekCount:2,
		dayCount:4,
		"program":[
			{
				"name":"bench",
				"reps":"5*62.5(115), 3(1)*72.5(135), 3*77.5(142.5), 3*4*82.5(152.5)",
			},
			{
				"name":"fly",
				"reps":"3*10"
			},
			{
				"name":"stand press",
				"reps":"3*6"
			},
		]
	},
	{
		weekCount:2,
		dayCount:4,
		"program":[
			{
				"name":"bench",
				"reps":"5*62.5(115), 3(1)*72.5(135), 3*77.5(142.5), 3*4*82.5(152.5)",
			},
			{
				"name":"fly",
				"reps":"3*10"
			},
			{
				"name":"stand press",
				"reps":"3*6"
			},
		]
	},
	{
		weekCount:3,
		dayCount:1,
		"program":[
			{
				"name":"bench",
				"reps":[
					['8',62.5],
					['8*4',67.5],
					['1',72.5],

				]
			},
			{
				"name":"fly",
				"reps":"3*10"
			},
			{
				"name":"stand press",
				"reps":"3*6"
			},
		]
	},
]
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
		res = res + (rep[0] + '(' + FloorUnit(max * rep[1],1.25)+')') + ', ';
	}
	return res;
}
function TwoSignsAfterComma(x){
	return (Math.floor(100.0*x))/100.0;
}
function FloorUnit(x,unit){
	return (Math.floor(x/unit))*unit;
}
