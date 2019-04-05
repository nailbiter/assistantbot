.PHONY: all \
	offline  botmanager trello interactive dpmanager mathmanager\
	 pull jar add \
	 params \
	 readme


#global const's
JARSUFF=-jar-with-dependencies
RESFOLDER=src/main/resources/assistantBotFiles/
REBOOTFILE=tmp/restart.txt
SECRET=secret.txt
MAINCLASS=Main
BOGUS=HHOOMMEE
USERRECORDS=src/main/resources/params src/main/resources/params/userRecords.json
MAKEFILESDIR=makefiles
#global var's
JARNAME=assistantBot-0.0.1-SNAPSHOT$(JARSUFF)
RUN=java -classpath $(subst $(BOGUS),$(shell echo ~),$(shell cat cp.txt)) $(MAINCLASS) $(KEYS) $(PERLKEYS)
KEYS=--password `cat $(SECRET)`
PERLKEYS=--tmpfile $(REBOOTFILE)
#procedures
all: src/main/resources/profiles/telegram.json target/$(JARNAME).jar
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>&1 | tee log/log.telegram.txt

#main
include $(MAKEFILESDIR)/Makefile.sources
include $(MAKEFILESDIR)/Makefile.dbdata
mathmanager: src/main/resources/profiles/mathmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" --daemonize --stderr log/log.$@.txt --pidfile tmp/pidfile.txt $< 
dpmanager: src/main/resources/profiles/dpmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" --daemonize --stderr log/log.$@.txt --pidfile tmp/pidfile.txt $< 
botmanager: src/main/resources/profiles/botmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>&1 | tee log/log.$@.txt
trello: src/main/resources/profiles/trello.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" --stderr log/log.$@.txt --pidfile tmp/pidfile.txt $< 
interactive: src/main/resources/profiles/interactive.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.interactive.txt
offline: src/main/resources/profiles/offline.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.offline.txt
target/$(JARNAME).jar : $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml cp.txt
	mvn compile
	touch $@
pull:
	git pull
	cd src/main/java/com/github/nailbiter/util && git pull
jar: $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml cp.txt
	mvn compile
	touch target/$(JARNAME).jar

#FILES
cp.txt: src/main/pl/parseCp.pl pom.xml
	mvn exec:exec -Dexec.executable="echo" -Dexec.args="%classpath" | perl  $< --bogus $(BOGUS) > $@
README.html: README.md
	markdown $< > $@
readme:
	make -C READMEs/
