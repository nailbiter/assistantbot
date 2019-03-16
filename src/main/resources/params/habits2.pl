makeHabit "*/2 * * * *", "test", -1, callback=>{name=>'managers.NewTrelloManager',method=>'report'},enabled=> 0, onFailed=>'putlabel' ;
