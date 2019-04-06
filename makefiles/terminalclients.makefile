trello: src/main/resources/profiles/trello.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" --tmpfile tmp/$@.restart.txt --stderr log/log.$@.txt --pidfile tmp/pidfile.txt $< 
interactive: src/main/resources/profiles/interactive.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.interactive.txt
offline: src/main/resources/profiles/offline.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.offline.txt
