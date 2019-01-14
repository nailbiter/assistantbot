var res = [
	{
		name:"managers.MoneyManager",
		parameter:{
			categories:["food","fun"],
			decsigns:0,
		},
	},
	{
		name:"managers.BirthdayManager",
		parameter:{
			birthdays:[
				{
					month:01,
					date:15,
					info:'www.google.com',
					name:'Test von Test',
					enabled:true,
				},
			]
		},
	},
];

console.log(JSON.stringify(res));
