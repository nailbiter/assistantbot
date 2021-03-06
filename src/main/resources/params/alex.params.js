const program = require('./gym/getprogram');
const birthdays = require('./birthday');

var res = [
	{
		name:"managers.OcrManager",
		parameter:{
			lang:"chi_tra",
		},
	},
	{
		name:"managers.GymManager",
		parameter:{
			//weekCount:2,
			program,
		},
	},
	{
		name:"managers.MoneyManager",
		parameter:{
			categories:["food","fun"],
			decsigns:0,
		},
	},
	{
		name:"managers.HabitManager",
		parameter:{
			pendingListName:"PENDING",
		},
	},
	{
		name:"managers.BirthdayManager",
		parameter:{
			birthdays
		},
	},
	{
		name:"managers.TaskManager",
		parameter:{
			"INBOX":[
				{
					board:"foFETfOx",
					list:"inbox",
					filters:[
						{
							type:"SETSEGMENT",
							data:1,
						},
					],
				},
				{
					board:"foFETfOx",
					list:"Prog",
					filters:[
						{
							type:"HEAD",
							data:2,
						},
						{
							type:"ADDLABEL",
							data:"coding",
						},
					],
				},
				{
					board:"foFETfOx",
					list:"sweet tasks",
					filters:[
						{
							type:"HEAD",
							data:2,
						},
						{
							type:"ADDLABEL",
							data:"logistics",
						},
					],
				},
				{
					board:"mQj3bkPA",
					list:"Alex TODO",
					filters:[
						{
							type:"ADDLABEL",
							data:"social",
						},
					],
				},
				{
					board:"nqI8xwIu",
					list:"TODO: code",
					filters:[
						{
							type:"HASDUE",
							data:null,
						},
						{
							type:"ADDLABEL",
							data:"parttime",
						},
					],
				},
			],
		},
	},
];

console.log(JSON.stringify(res));
