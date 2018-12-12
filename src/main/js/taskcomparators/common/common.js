//global var's
var recognizedCats = getVar('recognizedCats');
var catWeights = {
	//sleeping : 1,
	parttime: 2,
	logistics: -4,
	gym: 4,
	reading: 5,
	work: -6,
	rest: 7,
	social: -5,
	useless: 9,
	german: 10,
	coding: 100,
	"math project": 12,
	future: -7,
};

//procedures
function compare(obj1,obj2){
	var comparisonArray = [compareLabel,compareName];
	for(var i = 0; i < comparisonArray.length; i++ ){
		var res = comparisonArray[i](obj1,obj2);
		if( res != 0 )
			return JSON.stringify(res);
	}
	return JSON.stringify(0);
}
function compareLabel(o1,o2){
	var l1 = getMainLabel(o1),
		l2 = getMainLabel(o2);
	log("l1: "+l1);
	log("l2: "+l2);

	var b1 = l1 in catWeights,
		b2 = l2 in catWeights;
	if( b1 && b2 ){
		return intcmp(catWeights[l1],catWeights[l2]);
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
function intcmp(a,b){
    return (a<b?-1:(a>b?1:0));  
}
function getVar(key){
	return JSON.parse(ScriptHelper.execute(key));
}
