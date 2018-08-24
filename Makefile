.PHONY: all zip dryrun

JARNAME=assistantBot-0.0.1-SNAPSHOT-jar-with-dependencies
RESFOLDER=src/main/resources/
LOGFILE=log/log.txt
KEYS=-r $(RESFOLDER) -t bottoken -n AlexCovenBot

#sources
UTILSOURCES=KeyRing LocalUtil MyBasicBot MyManager Parser StorageManager TableBuilder UserData Util 
MAINSOURCES=HabitManager JShellManager Main MoneyManager MyAssistantBot MyAssistantUserData TimeManager 
SOURCES=$(addprefix util/,$(UTILSOURCES)) $(MAINSOURCES) opts/Option

all: target/$(JARNAME).jar
	java -jar $< $(KEYS) 2>&1 | tee $(LOGFILE)
dryrun: 
	java -jar target/$(JARNAME).jar $(KEYS) 2>&1 | tee $(LOGFILE)
zip: botmanager.zip
	unzip -l $<
	du -hs $<


botmanager.zip : $(addprefix $(RESFOLDER), keyring.json) target/$(JARNAME).jar Makefile
	zip -9 $(basename $@) $^
target/$(JARNAME).jar : $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES)))
	mvn package
