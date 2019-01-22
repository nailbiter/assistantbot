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
			weekCount:5,
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
		name:"managers.BirthdayManager",
		parameter:{
			birthdays:[
				{
					month:01,
					date:15,
					info:'www.google.com',
					name:'Test von Test',
					enabled:false,
				},
				{
					month:09,
					date:01,
					name:'Kolya Servetnik',
				},
				{
					month:09,
					date:24,
					name:'Lesya Zakoretskaya',
					info:'https://www.facebook.com/alesia.zakoretska',
				},
				{
					month:09,
					date:24,
					name:'Mike Wasilewich',
				},
				{
					month:09,
					date:20,
					name:'Naboychenko Sergii',
					info:'https://www.facebook.com/Serega.does.not.exist/',
				},
				{
					month:10,date:01,
					name:'Dad',
				},
				{
					month:08, date:24,
					name:'mom',
				},
				{
					month:09, date:23,
					name:'masha',
				},
				{
					month:02, date:23,
					name:'brother mike',
				},
				{
					month:09, date:13,
					name:'Aunt Anya',
				},
				{
					month:07, date:29,
					name:'Uncle Vanya',
				},
				{
					month:05, date:31,
					name:'Yura Gavrilenko',
				},
				{
					month:04, date:30,
					name:'Uncle Vlad',
					info:'https://www.facebook.com/profile.php?id=100010184923548',
				},
				{
					month:05, date:08,
					name:'Aunt Ira',
					info:'https://www.facebook.com/profile.php?id=100000057449112',
				},
				{
					month:12, date:22,
					name:'Aleksandros',
					info:'https://www.facebook.com/aleksandrs.dmitrenko',
				},
				{
					month:03, date:14,
					name:'Natalia Danovich',
					info:'https://www.facebook.com/profile.php?id=100001636841686',
				},
				{
					month:11, date:22,
					name:'Liu BeiBei',
					info:'https://www.facebook.com/profile.php?id=100006490411915',
				},
				{
					month:03, date:06,
					name:'Maslechkin Vasil',
					info:'https://www.messenger.com/t/MaslechkinVasiliy',
				},
				{
					name:'Sean Lin',
					month:12,date:04,
				},
				{
					name:'Ira Ansi',
					month:12,date:18,
					info:'https://www.facebook.com/ira.ansi',
				},
			]
		},
	},
];

console.log(JSON.stringify(res));
