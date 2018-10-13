.PHONY: all offline pull jar

JARNAME=assistantBot-0.0.1-SNAPSHOT-jar-with-dependencies
RESFOLDER=src/main/resources/assistantBotFiles/
LOGFILE=log/log.txt
KEYS=-r $(RESFOLDER) -n AssistantBot -p `cat secret.txt`

#sources
ASBOTSOURCES=MyAssistantUserData MyAssistantBot
MANAGERSOURCES=$(addsuffix Manager,Time Money Test MiscUtil Habit German)
UTILSOURCES=StorageManager TrelloAssistant
HABITMANAGERSOURCES=HabitManagerBase JSONObjectCallback
SHELLSOURCES=InteractiveShell
TESTSOURCES=UrlTest Test ParadigmTest
SOURCES=\
 $(addprefix assistantbot/,$(ASBOTSOURCES))\
 $(addprefix managers/,$(MANAGERSOURCES))\
 $(addprefix shell/,$(SHELLSOURCES))\
 $(addprefix util/,$(UTILSOURCES))\
 $(addprefix managers/tests/,$(TESTSOURCES))\
 $(addprefix managers/habits/,$(HABITMANAGERSOURCES))\
 managers/tests/ParadigmTest\
 opts/Option Main


all: target/$(JARNAME).jar
	make -C src/main/resources/assistantBotFiles files
	java -jar $< $(KEYS) 2>&1 | tee $(LOGFILE)
offline: target/$(JARNAME).jar
	java -jar $< -o remote $(KEYS) 2>&1 | tee $(LOGFILE)

target/$(JARNAME).jar : $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml
	mvn package
pull:
	git pull
jar:
	mvn package
