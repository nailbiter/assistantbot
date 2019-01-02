//global const's
var DEBUG = false;
var PRINTMETHOD = "printStatTableHTML";
var OPTIONSFULLNAMES = {
	d:"databaseName",
	u:"unit",
	s:"startIndex",
	e:"endIndex",
	a:'absolute',
	h:'help',
    o:'outputtype',
};
var DATEWEEKDAYBINDING = {
    weekday: 0,//monday
    year: 2018,
    month: 9,//october
    date: 01,
};
var RECORDTOKEY = {
	RECORD:function(record){
		return record["_id"]["$oid"];
	},
	DAY:function(record){
		var date = new Date(record.date['$date']);
		return date.getDate() + '/' + MONTHNAMES[date.getMonth()] + '/' + date.getFullYear();
	},
	MONTH:function(record){
		var date = new Date(record.date['$date']);
		return MONTHNAMES[date.getMonth()] + '/' + date.getFullYear();
	},
	WEEK:function(record){
		var date = new Date(record.date['$date']);
        //print((1900+date.getYear())+"/"+(1+date.getMonth())+"/"+date.getDate());
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            date.setMilliseconds(0);
        var bindedDate = new Date(DATEWEEKDAYBINDING.year,DATEWEEKDAYBINDING.month,DATEWEEKDAYBINDING.date,0,0,0,0);
        var dayNum = DATEWEEKDAYBINDING.weekday + days_between(bindedDate,date);
        var res = Math.floor(dayNum / 7.0)+"";
        if(DEBUG)
            print(formatDate(bindedDate,true)+" "+formatDate(date,true)+" => "+dayNum+" "+res);
        return res;
	},
};
var RECORDDISPATCHTABLE = {
	time:{
		toObj:function(record){
			var res = {};
			res[record.category] = 1;
			return res;
		},
		valueToString:function(value){
			return (value/2.0)+"h";
		},
	},
	money:{
		toObj:function(record){
			var res = {};
			res[record.category] = record.amount;
			return res;
		},
		valueToString:function(x){
			x = x.toString();
            var pattern = /(-?\d+)(\d{3})/;
            while (pattern.test(x))
            x = x.replace(pattern, "$1,$2");
            return x;
		},
	},
}
var ENDDATEKEY = '$enddate';
var STARTDATEKEY = '$startdate';

function main(cmdline){
	var cmdLineObj = parseCommandLine(cmdline,OPTIONSFULLNAMES);
	cmdLineObj.endIndex = Number(cmdLineObj.endIndex);
	setDefaultValues(cmdLineObj,{
		startIndex:0,
		databaseName:"time",
		unit:"RECORD",
		absolute:false,
		help:false,
        outputtype:"html"
	});
	cmdLineObj.unit = cmdLineObj.unit.toUpperCase();
	cmdLineObj.outputtype = cmdLineObj.outputtype.toUpperCase();

	dataString = ScriptHelper.execute(JSON.stringify({
			method:"getDataFromDatabase",
			data:{
					dbname:cmdLineObj.databaseName,
					sort:{date:-1},
			}
	}));
	var data = JSON.parse(dataString);
    if(DEBUG)
	    print("data.length: "+data.length);

    setDefaultValues(cmdLineObj,{
        endIndex:data.length,
    });
    if(DEBUG)
	    print("cmdLineObj: "+JSON.stringify(cmdLineObj));

	var res = [];
	if(cmdLineObj.help){
		printHelp();
	}
	else if(cmdLineObj.absolute){
		//TODO: add here
	}else{
		var key = '', oldKey = recordToKey(data[0],cmdLineObj.unit);
		var count = 0;
		var total = {};
		var dates = {};
		var callback = function(){
            if(DEBUG)
			    print("key="+key+", oldKey="+oldKey+", count="+count);
			oldKey = key;
			if(cmdLineObj.startIndex<=count && count<cmdLineObj.endIndex){
                if(DEBUG)
				    print("push");
				res.push(total);
			}
			count++;
			total = {};
		};
		for(var i = 0; i < data.length; i++){
			key = recordToKey(data[i],cmdLineObj.unit);
			if(oldKey!==key)
				callback();
			if(!(ENDDATEKEY in total))
				total[ENDDATEKEY] = data[i].date['$date'];
			total[STARTDATEKEY] = data[i].date['$date'];
			addToObj(total, recordToObj(data[i],cmdLineObj.databaseName));
		}
		callback();
	}

    var printedTable = 
        ScriptHelper.execute(JSON.stringify({
        method:("printStatTable"+cmdLineObj.outputtype),
        data:{
            data:res,
            colname:cmdLineObj.databaseName,
        },
    }));
    var tofile = printedTable;
    /*ScriptHelper.execute(JSON.stringify({
        method:"sendAsFile",
        data:{
            content:tofile,
        },
    }));*/
    return tofile;
}
function recordToObj(record,databaseName){
	return RECORDDISPATCHTABLE[databaseName].toObj(record);
}
function recordToKey(record,unit){
	return RECORDTOKEY[unit](record);
}
function printHelp(){
	print("help: ");
	print("command line keys:");
	for(key in OPTIONSFULLNAMES)
		print(key+":\t"+OPTIONSFULLNAMES[key]);
	print("\ntime units:");
	for(key in RECORDTOKEY)
		print("\t"+key);
}
function recordValueToString(value,databaseName){
	return RECORDDISPATCHTABLE[databaseName].valueToString(value);
}
