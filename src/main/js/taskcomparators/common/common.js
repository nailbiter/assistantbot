//global var's
var recognizedCats = getVar('recognizedCats');
//global var's
var recognizedCats = getVar('recognizedCats');
var catWeights = {
	//sleeping : 1,
	parttime: -5,
	logistics: -4,
	gym: 4,
	reading: 5,
	work: -6,
	rest: 7,
	social: -4,
	useless: 9,
	german: 10,
	coding: 100,
	"math project": 12,
	future: -7,
};

//procedures
function compare(obj1,obj2){
	var comparisonArray = [compareLabel,compareDate,compareName];
	for(var i = 0; i < comparisonArray.length; i++ ){
		var res = comparisonArray[i](obj1,obj2);
		if( res != 0 )
			return JSON.stringify(res);
	}
	return JSON.stringify(0);
}
function compareDate(o1,o2){
	var b = [o1.due==null,o2.due==null];
	if( b[0] && b[1]){
		return 0;
	} else if(b[0] && !b[1]) {
		return 1;
	} else if(!b[0] && b[1]){
		return -1;
	} else {
		var d1 = talkToHelper({
			cmd:"daysTill",
			data:o1.due,
		}), d2 = talkToHelper({
			cmd:"daysTill",
			data:o2.due,
		});
		if(d1<0 && d2<0){
			return numcmp(-d1,-d2);
		} else {
			return numcmp(d1,d2);
		}
	}
}
function talkToHelper(x){
	return JSON.parse(ScriptHelper.execute(JSON.stringify(x)));
}
function compareLabel(o1,o2){
	var l1 = getMainLabel(o1),
		l2 = getMainLabel(o2);
	log("l1: "+l1);
	log("l2: "+l2);

	var b1 = l1 in catWeights,
		b2 = l2 in catWeights;
	if( b1 && b2 ){
		return numcmp(catWeights[l1],catWeights[l2]);
	} else if( b1 && !b2) {
		return 1;
	} else if( !b1 && b2) {
		return -1;
	} else {
		return strcmp(l1,l2);
	}
}
function log(msg){
	ScriptHelper.execute(JSON.stringify({
		cmd:"log",
		data:msg,
	}));
}
function getMainLabel(o){
	log('recognizedCats: '+JSON.stringify(recognizedCats));
	for(var i = 0; i < o.labels.length; i++){
		var label = o.labels[i];
		log('label: '+label);
		if( recognizedCats.indexOf(label) >= 0 )
			return label;
	}
}
function compareName(o1,o2){
	return strcmp(o1.name,o2.name)
}
function strcmp(a, b)
{   
    return (a<b?-1:(a>b?1:0));  
}
function numcmp(a,b){
    return (a<b?-1:(a>b?1:0));  
}
function getVar(key){
	return JSON.parse(ScriptHelper.execute(key));
}
