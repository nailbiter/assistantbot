//global var's
var recognizedCats = getVar('recognizedCats');
var catWeights = {
	//sleeping : 1,
	parttime: 2,
	logistics: 3,
	gym: 4,
	reading: 5,
	work: -6,
	rest: 7,
	social: 8,
	useless: 9,
	german: 10,
	coding: 11,
	"math project": 12,
	future: 13,
};

//procedures
function compare(obj1,obj2){
	//var res = compareLabel(obj1,obj2);
	var res = compareName(obj1,obj2);
	return JSON.stringify(res);
}
function compareLabel(o1,o2){
	var l1 = getMainLabel(o1),
		l2 = getMainLabel(o2);
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
function getMainLabel(o){
	for(label in o.labels)
		if( recognizedCats.indexOf(label) >= 0 )
			return label;
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
