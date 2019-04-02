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
			weekCount:1,
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
				/*{
					board:"nqI8xwIu",
					list:"TODO: code",
					filters:[
					],
				},*/
			],
		},
	},
];

console.log(JSON.stringify(res));
