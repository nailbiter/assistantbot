mathmanager: src/main/resources/profiles/mathmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./$(PERLDIR)/run.pl --cmd "$(RUN) $<" --daemonize --stderr log/log.$@.txt --pidfile tmp/pidfile.txt $< 
dpmanager: src/main/resources/profiles/dpmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./$(PERLDIR)/run.pl --cmd "$(RUN) $<" --daemonize --stderr log/log.$@.txt --pidfile tmp/pidfile.txt $< 
botmanager: src/main/resources/profiles/botmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./$(PERLDIR)/run.pl --cmd "$(RUN) $<" --daemonize --stderr log/log.$@.txt --pidfile tmp/pidfile.txt $< 
