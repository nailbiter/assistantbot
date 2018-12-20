//procedures
function main(cmdline){
  var array = [];
  for(var i = 0; i < 32; i++){
    array.push({
      key:padToTwo(i+1),
      probability:1.0,
    });
  }

	return JSON.stringify(array);
}
function padToTwo(num){
	if(0<=num && num<10)
		return '0'+num;
	else
		return ''+num;
}
