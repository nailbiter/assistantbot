
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
	{
		weekCount:5,
		dayCount:1,
		program:[
			{
				"name":"bench",
				"reps":[
					['8x3',62.5],
					['8x4',70],
					['1*',77.5],
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
		weekCount:5,
		dayCount:4,
		program:[
			{
				name:"bench",
				reps:[
					['5',62.5],
					['5',72.5],
					['1*',77.5],
					['5x4',80],
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
		weekCount:weekShift(3),
		dayCount:2,
		program:[
			{
				name:"brus",
				reps:"2*12, 3*27.5k*10x"
			},
			{
				name:"razg",
				reps:"2*15*12,5x, 3*10*17.5k"
			},
			{
				name:"bic",
				reps:"2*15*12.5k, 3*10*15.k"
			},
			{
				name:"molot",
				reps:"2*15*15k, 3*12*22.5k"
			}
		]
	},
	{
		weekCount:weekShift(3),
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"3*10x*35k, 2*8x*40k"
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
		weekCount:6,
		dayCount:1,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
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
		weekCount:weekShift(4),
		dayCount:2,
		program:[
			{
				name:"brus",
				reps:"2*12, 3*30k*8x"
			},
			{
				name:"rsid",
				reps:"2*15*12k,5x, 3*10*14k"
			},
			{
				name:"bic",
				reps:"2*15*12.5k, 3*10*15k"
			},
			{
				name:"molot",
				reps:"2*15*15k, 3*12*22.5k"
			}
		]
	},
	{
		weekCount:weekShift(4),
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"3*10x*35k, 2*8x*40k"
			},
			{
				name:"grud",
				reps:"3x5x35"
			},
			{
				name:"squat",
				reps:[
					['8x*2',77],
					['6x*1',79],
					['5x*2',81],
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
		weekCount:6,
		dayCount:4,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['3',72.5],
					['3(1*)',80.0],
					['3*3',85.0],
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
		weekCount:7,
		dayCount:1,
		program:[
			{
				"name":"bench",
				"reps":[
					['8',62.5],
					['8*4',72.5],
					['1',82.5],
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
		weekCount:weekShift(5),
		dayCount:2,
		program:[
			{
				name:"brus",
				reps:"2*12, 3*30k*10x"
			},
			{
				name:"razg",
				reps:"2*15*12k,5x, 3*10*14k"
			},
			{
				name:"bic",
				reps:"2*15*12.5k, 3*10*15k"
			},
			{
				name:"molot",
				reps:"2*15*15k, 3*12*22.5k"
			}
		]
	},
	{
		weekCount:weekShift(5),
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"2*10x*35k, 3*8x*40k"
			},
			{
				name:"grud",
				reps:"3x5x35"
			},
			{
				name:"squat",
				reps:[
					['8x*2',77],
					['6x*1',79],
					['5x*2',81],
				],
			},
			{
				name:"stan",
				reps:"3x8x50"
			},
			{
				name:"matr",
				"reps":"61*12x*3"
			},
			{
				name:"pullup",
				reps:"2*Max"
			}
		]
	},
	{
		weekCount:7,
		dayCount:4,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
					['5*4',80.0],
					['1',82.5],
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
		weekCount:8,
		dayCount:1,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
					['5*3',82.5],
					['1*',85.0],
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
		weekCount:weekShift(6),
		dayCount:2,
		program:[
			{
				name:"brus",
				reps:"2*12, 3*30k*10x"
			},
			{
				name:"rsid",
				reps:"2*15*12k,5x, 3*10*14k"
			},
			{
				name:"bic",
				reps:"2*15*12.5k, 3*10*15k"
			},
			{
				name:"molot",
				reps:"2*15*15k, 3*12*22.5k"
			}
		]
	},
	{
		weekCount:weekShift(6),
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"2*10x*35k, 3*8x*40k"
			},
			{
				name:"grud",
				reps:"3x4x37.5"
			},
			{
				name:"squat",
				reps:[
					['8x*2',77],
					['6x*1',79],
					['5x*2',81],
				],
			},
			{
				name:"stan",
				reps:"3x6x60"
			},
			{
				name:"matr",
				"reps":"61*12x*3"
			},
			{
				name:"pullup",
				reps:"2*Max"
			}
		]
	},
	{
		weekCount: 8,
		dayCount:4,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
					['1*',85.0],
					['2/3',87.5],
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
		weekCount: 9,
		dayCount:1,
		program:[
			{
				"name":"bench",
				"reps":[
					['8',65],
					['8/4',75],
					['1*',87.5],
				]
			},
			{
				"name":"fly",
				"reps":"3*8"
			},
			{
				"name":"stand press",
				"reps":"1*5,4,3"
			},
		]
	},
	{
		weekCount: 9,
		dayCount:2,
		program:[
			{
				name:"brus",
				reps:"2*12, 3*32k*8x"
			},
			{
				name:"rsid",
				reps:"2*15*12k,5x, 3*10*14k"
			},
			{
				name:"bic",
				reps:"2*15*12.5k, 3*10*15k"
			},
			{
				name:"molot",
				reps:"2*15*15k, 3*12*22.5k"
			}
		]
	},
	{
		weekCount: 9,
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"2*10x*35k, 3*8x*40k"
			},
			{
				name:"grud",
				reps:"2x4x37.5, 1x3x40"
			},
			{
				name:"squat",
				reps:[
					['5x*2',81],
					['4x*1',84],
					['3x*2',86],
				],
			},
			{
				name:"stan",
				reps:"3x3x70"
			},
			{
				name:"matr",
				"reps":"61*12x*3"
			},
			{
				name:"pullup",
				reps:"3*Max"
			}
		]
	},
	{
		weekCount: 9,
		dayCount:4,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
					['5/3',82.5],
					['1*',87.5],
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
		weekCount: 10,
		dayCount:3,
		program:[
			{
				name:"tricPull",
				"reps":"1*10x*35k, 4*8x*40k"
			},
			{
				name:"grud",
				reps:"2x4x37.5, 2x3x40"
			},
			{
				name:"squat",
				reps:[
					['6x*1',79],
					['5x*1',81],
					['4x*2',84],
				],
			},
			{
				name:"stan",
				reps:"3x5x65"
			},
			{
				name:"matr",
				"reps":"61*12x*3"
			},
			{
				name:"pullup",
				reps:"3*Max"
			}
		]
	},
	{
		weekCount: 10,
		dayCount:1,
		program:[
			{
				"name":"bench",
				"reps":[
					['5',62.5],
					['5',72.5],
					['5x3',85],
					['1*',90],
				]
			},
			{
				"name":"fly",
				"reps":"3*8"
			},
			{
				"name":"stand press",
				"reps":"1*5,4,3"
			},
		]
	},
]
module.exports = program;
