.PHONY: all \
	offline  botmanager trello interactive dpmanager\
	 pull jar add \
	 habits params \
	 readme

#JARSUFF=-jar-with-dependencies
JARNAME=assistantBot-0.0.1-SNAPSHOT$(JARSUFF)
RESFOLDER=src/main/resources/assistantBotFiles/
LOGFILE=log/log.txt
REBOOTFILE=tmp/restart.txt
SECRET=secret.txt
KEYS=--password `cat $(SECRET)`
PERLKEYS=--tmpfile $(REBOOTFILE)
MAINCLASS=Main
BOGUS=HHOOMMEE
USERRECORDS=src/main/resources/params src/main/resources/params/userRecords.json
RUN=java -classpath $(subst $(BOGUS),$(shell echo ~),$(shell cat cp.txt)) $(MAINCLASS) $(KEYS) $(PERLKEYS)
PERL=perl -I ~/perl5/lib/perl5

all: src/main/resources/profiles/telegram.json target/$(JARNAME).jar
	mkdir -p tmp
	rm -rf $(REBOOTFILE)
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>&1 | tee log/log.telegram.txt

#PHONY
include Makefile.sources
include Makefile.dbdata
dpmanager: src/main/resources/profiles/dpmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	mkdir -p tmp
	rm -rf $(REBOOTFILE)
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>&1 | tee log/log.$@.txt
botmanager: src/main/resources/profiles/botmanager.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	mkdir -p tmp
	rm -rf $(REBOOTFILE)
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>&1 | tee log/log.$@.txt
trello: src/main/resources/profiles/trello.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.$@.txt
interactive: src/main/resources/profiles/interactive.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
	$(PERL) ./src/main/pl/run.pl --cmd "$(RUN) $<" $(PERLKEYS) 2>log/log.interactive.txt
offline: src/main/resources/profiles/offline.json target/$(JARNAME).jar $(SECRET) $(USERRECORDS)
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

#FILES
cp.txt: src/main/pl/parseCp.pl pom.xml
	mvn exec:exec -Dexec.executable="echo" -Dexec.args="%classpath" | perl  $< --bogus $(BOGUS) > $@
README.html: README.md
	markdown $< > $@
readme:
	make -C READMEs/
