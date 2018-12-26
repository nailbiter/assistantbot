var MONTHNAMES = [
      "January", "February", "March",
      "April", "May", "June", "July",
      "August", "September", "October",
      "November", "December"
    ];
function setDefaultValues(obj,defaultObj){
	for(var prop in defaultObj)
		if(!(prop in obj))
			obj[prop] = defaultObj[prop];
}
function addToObj(obj,incObj){
	for(cat in incObj){
		if(!(cat in obj))
			obj[cat] = 0;
		obj[cat] += incObj[cat];
	}
}
function formatDate(date,isShort) {
	  var day = date.getDate();
	  var monthIndex = date.getMonth();
	  var year = date.getFullYear();

	  return padToTwo(day) + '/' + MONTHNAMES[monthIndex] + '/' + year +
        (isShort?"":(' '+padToTwo(date.getHours())+':'+padToTwo(date.getMinutes())));
}
function padToTwo(num){
	if(0<=num && num<10)
		return '0'+num;
	else
		return ''+num;
}
function parseCommandLine(cmdline,optionsFullNames){
	var split = cmdline.split(" ");
	var res = {};
	var waitingForArg = '';
	for(var i = 0; i < split.length; i++){
		if(split[i].startsWith('-')){
			if(waitingForArg.length > 0){
				res[optionsFullNames[waitingForArg]] = true;
			}
			waitingForArg = split[i].substring(1);
		} else {
			if(waitingForArg.length > 0){
				res[optionsFullNames[waitingForArg]] = split[i];
				waitingForArg = '';
			}
		}
	}
	if(waitingForArg.length > 0){
		res[optionsFullNames[waitingForArg]] = true;
	}
	return res;
}
function days_between(date1, date2) {
    var ONE_DAY = 1000 * 60 * 60 * 24;
    var date1_ms = date1.getTime();
    var date2_ms = date2.getTime();
    var difference_ms = (date2_ms - date1_ms);
    return Math.round(difference_ms/ONE_DAY);
}
