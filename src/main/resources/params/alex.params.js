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
			weekCount:4,
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
		name:"managers.NewTrelloManager",
		parameter: {
		},
	},
];

console.log(JSON.stringify(res));
