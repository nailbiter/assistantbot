//procedures
function main(cmdline){
    ScriptHelper.execute(JSON.stringify({
        method:"dropCollection",
        data:{
            dbname:"logistics.randsetdistrib",
        },
    }));
    var array = [];
    for(var i = 0; i < 25; i++){
        array.push({
            key:padToTwo(i+1),
            probability:1.0,
        });
    }

    var obj = {
        method:"addToDatabase",
        data:{
            dbname:"logistics.randsetdistrib",
            data:array,
        },
    };
    ScriptHelper.execute(JSON.stringify(obj));
}
