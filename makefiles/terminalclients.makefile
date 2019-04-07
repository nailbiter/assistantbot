trello: src/main/resources/profiles/trello.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./$(RUNPL) --cmd "$(RUN) $<" --pidfile tmp/pidfile.txt $< 2>log/log.$@.txt
interactive: src/main/resources/profiles/interactive.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./$(RUNPL) --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.$@.txt
offline: src/main/resources/profiles/offline.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./$(RUNPL) --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.$@.txt
