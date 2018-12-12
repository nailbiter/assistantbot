function compare(obj1,obj2){
	return JSON.stringify(strcmp(obj1.name,obj2.name));
}

function strcmp(a, b)
{   
    return (a<b?-1:(a>b?1:0));  
}
