makeHabit "0 9 * * *", "test", -1, callback=>{name=>'managers.NewTrelloManager',method=>'report'},enabled=> 0, onFailed=>'putlabel' ;
