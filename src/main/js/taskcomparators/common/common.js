function compare(obj1,obj2){
	//var obj1 = JSON.parse(ScriptHelper.execute("a")),
	//	obj2 = JSON.parse(ScriptHelper.execute('b'));
	return JSON.stringify(strcmp(obj1.name,obj2.name));
}
function strcmp(a, b)
{   
    return (a<b?-1:(a>b?1:0));  
}
