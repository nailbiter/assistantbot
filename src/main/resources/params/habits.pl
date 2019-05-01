#makehabits "08:00", "10:00", 2, "read main 1 1page", 1080, category => "reading";
#makehabits "08:00", "10:00", 1, "read main 2 1page", 1080, category=> "reading";
#makehabits "08:00", "14:00", 3, "do main task", 1080, category=>"tasks", onFailed=>"move";
#makeHabit "30 23 * * *", "take pills", 30, category=>"logistics", onFailed=>"remove";
makeHabit "30 23 * * *", "check/plan tasks", 30, category=>"tasks", onFailed=>"remove";
makeHabit "0 23 * * *", "good day", 30 , category=>"logistics", info=>"https://trello.com/c/ilo6JlQO", onFailed=>"remove";
makeHabit "0 21 * * *", "drink protein", 3*60 , category=>"logistics", onFailed=>"remove";
makeHabit "0 10 * * *", "duolingo" 23*60 , category=>"german", onFailed=>"remove";

makeHabit "0 10 * * *", "math reading", 1080, items=>["main1 1 page","main1 1 page","main2 1 page",], onFailed=>'move:FAILED2';
makeHabit "0 11 * * *", "talk to someone in person",  1080,  category=>"social", onFailed=>"remove";
makeHabit "0 10 * * *", "8 random test", 1080, category=> "german",items=>["1","2","3","4","5",'6','7','8'];
makeHabit "0 10 * * *", "einsoch6 SET", 1080, category=> "german", info=> "http://www.dw.com/de/deutsch-lernen/das-bandtagebuch-1-lass-uns-reden/s-32658", onFailed=>'move:FAILED2';
makeHabit "30 14 * * fri", "mom talk", 1080, category=> "social" ;
makeHabit "0 22 * * fri", "granny talk", 1080, category=> "social" ;
#makeHabit "30 23 * * sat", "masha report", 1080, category=> "social", onFailed=>"remove";
#makeHabit "30 23 * * mon", "masha deadline", 1080, category=> "social" ;
#makeHabit "0 16 * * *", "masha remind", 1080, category=> "social" ;
#makeHabit "0 15 * * *", "pevzner 1h", 1080, onFailed=> "move" ;
makeHabit "0 15 * * *", "uncle vanya project", 1080, onFailed=> "move:FAILED",info=>'https://trello.com/c/qGE98nSa';
makeHabit "0 16 * * mon", "dad english", 1080, onFailed=>'move:TODO' ;
makeHabit "30 23 * * *", "mental exercise", 1080, onFailed=>'move:FAILED2';
makeHabit "0 8 * * sat,sun", "sprint 4", 1380, category=> "gym" ;
makeHabit "0 8 * * wed", "long walk", 1380, category=> "gym" ;
makeHabit "0 7 * * *", "pullups 10:1", 1080, category=> "gym" ;
makeHabit "2 7 * * *", "pullups 10:2", 1080, category=> "gym" ;
makeHabit "4 7 * * *", "pullups 10:3", 1080, category=> "gym" ;
makeHabit "6 7 * * *", "pullups 10:4", 1080, category=> "gym" ;
makeHabit "0 19 * * fri", "clean ears", 1380 ;
#makeHabit "0 13 * * *", "elsa", 1380 ;
makeHabit "0 12 1 * *", "back-up K emails", 1380 ;
#makeHabit "0 10 * * *", "backup baito DB twice, check apaz/dulut time", 1380, onFailed=>'remove';
makeHabit "0 7 * * *", "morning note habit", 300 ,info=>"https://docs.google.com/document/d/1Q4wvok8I1AAc0Jtv6gU3d-MJK69uvsqCeLPbBvLiCTg/edit#";
makeHabit "0 7 * * *","face whipe, wash head, faceCream x 2", 300,category=>'logistics';
makeHabit "0 22 2,17 * *","shave penis, armpits; cut nails",60*24,category=>'logistics';
makeHabit "0 9 20 */2 *","haircut",60*24,category=>'logistics';
makeHabit "0 7 * * *", "apple", 300 ;
makeHabit "0 7 * * *", "do 3*9_LegParBar/45_abs", 1080, category=> "gym" ;
makeHabit "0 7 * * *", "do pushups", 1080, category=> "gym" ;
makeHabit "0 21 2,17 * *", "water tree", 60, info=>"water it once per two weeks by pouring water into the flowerpot; 3/4 of a cup";
makeHabit "0 22 * * *", "have sex with Candice", 60*3 ,category=>'social',onFailed=>"remove";
makeHabit "0 10 21 4,7,10,1 *", "change toothbrush", 60*24 ,category=>'logistics';
makeHabit "30 23 * * *", "sleep", 45, onFailed=> "remove" ,category=>'logistics';
makeHabit "0 7 * * *", "wake (should be out)", 75, onFailed=> "remove" ,category=>'logistics';
makeHabit "0 11 * * mon", "sync latex, texmacs and mac's mongodb", 1380 ;
makeHabit "0 8 * * *", "read math 2 hours", 960, enabled=> 0 ;
makeHabit "15 22 * * Fri", "confucius 1h: 1", 960, onFailed=>'move:FAILED2';
makeHabit "30 22 * * Fri", "confucius 1h: 2", 960, onFailed=>'move:FAILED2';
makeHabit "00 22 13 * *", "inflate bike", 960 , category=>"atschool";
makeHabit "00 10 * * mon", "check week stat (money, time)", 960 , category=>"logistics";
#makeHabit "00 10 */15 * *", "fix test.pl", 960 , category=>"logistics";
makeHabit "0 10 28 * *", "check salary, estimate income", 960;
#makeHabit "52 7 * * *", "test", 1, enabled=> 0, onFailed=>'putlabel' ;
#makeHabit "* * * * *", "test2", -1, callback=>{name=>'managers.NewTrelloManager',method=>'report'},enabled=> 0, onFailed=>'putlabel' ;
