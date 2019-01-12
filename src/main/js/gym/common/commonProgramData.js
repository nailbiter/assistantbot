function weekShift(x){
	return x+2;
}
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
		weekCount:2,
		dayCount:2,
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
		weekCount:2,
		dayCount:3,
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
		program:[
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
	{
		weekCount:1+2,
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"4*10x*35k, 1*8x*40k"
			},
			{
				name:"grud",
				reps:"3x5x30"
			},
			{
				name:"squat",
				reps:[
					['5x10',72],
				],
			},
			{
				name:"stan",
				reps:"3x8x50"
			},
			{
				name:"matr",
				"reps":"28.5k*12x*3"
			},
			{
				name:"pullup",
				reps:"2xMax"
			}
		]
	},
	{
		weekCount:3,
		dayCount:4,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
					['1',75],
					['5x4',77.5],
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
	{
		weekCount:3,
		dayCount:2,
		program:[
			{
				name:"brus",
				reps:"2*12, 3*25k*10"
			},
			{
				name:"razg",
				reps:"2*15, 3*10"
			},
			{
				name:"bic",
				reps:"2*15, 3*10"
			},
			{
				name:"molot",
				reps:"2*15*15k, 3*12*22.5k"
			}
		]
	},
	{
		weekCount:4,
		dayCount:1,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
					['1*',77.5],
					['5x4',80.0],

				]
			},
			{
				"name":"fly",
				"reps":"3*10-12"
			},
			{
				"name":"stand press",
				"reps":"3*6"
			},
		]
	},
	{
		weekCount:weekShift(2),
		dayCount:2,
		program:[
			{
				name:"brus",
				reps:"2*12, 3*27.5k*8x"
			},
			{
				name:"rsid",
				reps:"2*15, 3*10"
			},
			{
				name:"bic",
				reps:"2*15, 3*10"
			},
			{
				name:"molot",
				reps:"2*15*15k, 3*12*22.5k"
			}
		]
	},
	{
		weekCount:weekShift(2),
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"4*10x*35k, 1*8x*40k"
			},
			{
				name:"grud",
				reps:"3x5x32.5"
			},
			{
				name:"squat",
				reps:[
					['8x*5',77],
				],
			},
			{
				name:"stan",
				reps:"3x8x50"
			},
			{
				name:"matr",
				"reps":"28.5k*12x*3"
			},
			{
				name:"pullup",
				reps:"2*Max"
			}
		]
	},
	{
		weekCount:4,
		dayCount:4,
		program:[
			{
				name:"bench",
				reps:[
					['5',62.5],
					['2',72.5],
					['1*',75],
					['2',77.5],
					['2',82.5],
					['2x3',85],
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
