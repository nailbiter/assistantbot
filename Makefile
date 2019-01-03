.PHONY: all offline pull jar add trello interactive habits users botmanager

#JARSUFF=-jar-with-dependencies
JARNAME=assistantBot-0.0.1-SNAPSHOT$(JARSUFF)
RESFOLDER=src/main/resources/assistantBotFiles/
LOGFILE=log/log.txt
REBOOTFILE=tmp/restart.txt
KEYS=--password `cat secret.txt`
PERLKEYS=--tmpfile $(REBOOTFILE)
MAINCLASS=Main
BOGUS=HHOOMMEE
RUN=java -classpath $(subst $(BOGUS),$(shell echo ~),$(shell cat cp.txt)) $(MAINCLASS) $(KEYS) $(PERLKEYS)
PERL=perl -I ~/perl5/lib/perl5

all: src/main/resources/profiles/telegram.json target/$(JARNAME).jar
	mkdir -p tmp
	rm -rf $(REBOOTFILE)
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>&1 | tee log/log.telegram.txt
include Makefile.sources

#PHONY
botmanager: src/main/resources/profiles/botmanager.json target/$(JARNAME).jar
	mkdir -p tmp
	rm -rf $(REBOOTFILE)
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>&1 | tee log/log.$@.txt
habits:
	make -C src/main/resources/habits
	./src/main/pl/uploadJson.pl --file src/main/resources/habits/habits.json --colname alex.habits
trello: src/main/resources/profiles/trello.json target/$(JARNAME).jar
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.$@.txt
interactive: src/main/resources/profiles/interactive.json target/$(JARNAME).jar
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.interactive.txt
offline: src/main/resources/profiles/offline.json target/$(JARNAME).jar
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.offline.txt
target/$(JARNAME).jar : $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml cp.txt
	mvn compile
	touch $@
pull:
	git pull
	cd src/main/java/com/github/nailbiter/util && git pull
jar: $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml cp.txt
	mvn compile
	touch target/$(JARNAME).jar
users: src/main/pl/updateUserRecords.pl src/main/resources/userRecords.json
	$(PERL) src/main/pl/updateUserRecords.pl --json src/main/resources/userRecords.json $(KEYS)

#FILES
cp.txt: src/main/pl/parseCp.pl pom.xml
	mvn exec:exec -Dexec.executable="echo" -Dexec.args="%classpath" | perl  $< --bogus $(BOGUS) > $@
README.html: README.md
	markdown $< > $@
