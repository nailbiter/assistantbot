//global const's
var COLLECTIONNAME = "timecats"
var COLLECTIONSDATA = [
    {
        name:"sleeping",
        canBePersistent:"nomessage",
    },
    {
        name:"parttime",
    },
    {
        name:"logistics",
    },
    {
        name:"gym",
    },
    {
        name:"reading",
    },
    {
        name:"work",
    },
    {
        name:"rest",
    },
    {
        name:"social",
        canBePersistent:"message",
    },
    {
        name:"useless",
        isDefault:true,
    },
    {
        name:"german",
    },
    {
        name:"coding",
    },
    {
        name:"math project",
    }
];
//procedures
function main(cmdline){
    ScriptHelper.execute(JSON.stringify({
        method:"dropCollection",
        data:{
            dbname:("logistics."+COLLECTIONNAME),
        },
    }));

    for(var i = 0; i < COLLECTIONSDATA.length; i++){
        setDefaultValues(COLLECTIONSDATA[i],{
            canBePersistent:"no",
            isDefault:false,
        });
    }

    var obj = {
        method:"addToDatabase",
        data:{
            dbname:("logistics."+COLLECTIONNAME),
            data:COLLECTIONSDATA,
        },
    };
    ScriptHelper.execute(JSON.stringify(obj));
}
