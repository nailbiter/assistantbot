const alex = require('./userRecords/alex');

var res = {
	alex,
	mariia:{
		managers:[
			"MoneyManager"
		],
		loginmessage:[
			"README:",
			"https://github.com/nailbiter/assistantbot/blob/master/READMEs/MoneyBot.rus.md"
		],
		timezone:"EET",
	},
	father:{
		managers:[
			"MoneyManager"
		],
		loginmessage:[
			"README:",
			"https://github.com/nailbiter/assistantbot/blob/master/READMEs/MoneyBot.rus.md"
		],
		timezone:"EET",
	},
};

console.log(JSON.stringify(res));
