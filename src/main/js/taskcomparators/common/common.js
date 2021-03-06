//global const's
var EXCLAMATION = "!";
//global var's
var recognizedCats = getVar('recognizedCats');
//TODO: write to DB
var catWeights = {
	//sleeping : 1,
	parttime: -5,
	logistics: -3,
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
function compareObjects(obj1,obj2) {
	var comparisonArray = [
    compareMainLabel,
    compareDate,
    compareExclamation,
    function(o1, o2) {
      var l1 = o1.labels,
        l2 = o2.labels,
        m1 = getMainLabel(o1),
        m2 = getMainLabel(o2);

      l1 = l1.filter(function(l) {
        return l !== m1;
      });
      l2 = l2.filter(function(l) {
        return l !== m2;
      })

      log("SOKM: "+l1.join(","));
      log("SOKM: "+l2.join(","));
      var res = strcmp(l1,l2);
      log("SOKM: "+res);

      return res;
    },
    compareName,
  ];
	for(var i = 0; i < comparisonArray.length; i++ ){
		var res = comparisonArray[i](obj1,obj2);
		if( res != 0 )
			return JSON.stringify(res);
	}
	return JSON.stringify(0);
}
function compareExclamation(o1,o2) {
	var i1 = 0, i2 = 0;
	log("compareExclamation "+JSON.stringify(o1.labels));
	log("compareExclamation "+JSON.stringify(o2.labels));

	for(var i = 0; i < o1.labels.length; i++){
		if( o1.labels[i] == EXCLAMATION )
			i1 = 1;
	}
	for(var i = 0; i < o2.labels.length; i++){
		if( o2.labels[i] == EXCLAMATION )
			i2 = 1;
	}

	var res = -(i1-i2);
	log('res: '+res);
	return res;
}
function compareDate(o1,o2){
	var b = [o1.due==null || o1.dueComplete,
		o2.due==null || o2.dueComplete];
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
function compareMainLabel(o1,o2){
	var l1 = getMainLabel(o1),
		l2 = getMainLabel(o2);
	log("getMainLabel: "+l1);
	log("getMainLabel: "+l2);

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
  return undefined;
}
function compareName(o1,o2) {
	return strcmp(o1.name,o2.name)
}
function strcmp(a, b) {
    return (a<b?-1:(a>b?1:0));
}
function numcmp(a,b){
    return (a<b?-1:(a>b?1:0));
}
function getVar(key){
	return JSON.parse(ScriptHelper.execute(key));
}
