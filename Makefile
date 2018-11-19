.PHONY: all offline pull jar gym

#JARSUFF=-jar-with-dependencies
JARNAME=assistantBot-0.0.1-SNAPSHOT$(JARSUFF)
RESFOLDER=src/main/resources/assistantBotFiles/
LOGFILE=log/log.txt
REBOOTFILE=tmp/restart.txt
RUNCOMMANDSFILE=src/main/resources/runcommands.json
KEYS=-r $(RESFOLDER) -n AssistantBot -p `cat secret.txt`
PERLKEYS=--tmpfile $(REBOOTFILE) --cmdfile $(RUNCOMMANDSFILE)
MAINCLASS=Main
RUN=java -classpath $(subst HHOOMMEE,$(shell echo ~),$(shell cat cp.txt)) Main

#sources
include Makefile.sources

all: target/$(JARNAME).jar
	make -C src/main/resources/assistantBotFiles files
	mkdir -p tmp
	rm -rf $(REBOOTFILE)
	./src/main/pl/run.pl --cmd "$(RUN) $(KEYS) -t $(REBOOTFILE) -c $(RUNCOMMANDSFILE)" $(PERLKEYS) 2>&1 | tee $(LOGFILE)
offline: target/$(JARNAME).jar
	$(RUN) -o local $(KEYS) 2>$(LOGFILE)
	#mvn exec:exec -Dexec.executable="echo" -Dexec.args="%classpath" 2>&1 |tee $(LOGFILE)
target/$(JARNAME).jar : $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml
	mvn compile
	touch $@
pull:
	git pull
	cd src/main/java/com/github/nailbiter/util && git pull
jar:$(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml
	mvn compile
	touch target/$(JARNAME).jar
gym:
	./src/main/pl/makeGym.pl --program src/main/resources/gym.json
