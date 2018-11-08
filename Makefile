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

#sources
ASBOTSOURCES=MyAssistantUserData MyAssistantBot
MANAGERSOURCES=$(addsuffix Manager,Time Money Test MiscUtil Habit German Gym Abstract)
UTILSOURCES=StorageManager TrelloAssistant MyBasicBot MongoUtil Util TelegramUtil
HABITMANAGERSOURCES=HabitManagerBase JSONObjectCallback
SHELLSOURCES=InteractiveShell
TESTSOURCES=UrlTest JsonTest ParadigmTest
MISCSOURCES=MashaRemind RandomSetGenerator NoteMaker
SOURCES=\
 $(addprefix assistantbot/,$(ASBOTSOURCES))\
 $(addprefix managers/misc/,$(MISCSOURCES))\
 $(addprefix managers/,$(MANAGERSOURCES))\
 $(addprefix shell/,$(SHELLSOURCES))\
 $(addprefix util/,$(UTILSOURCES))\
 $(addprefix com/github/nailbiter/util/,TrelloAssistant Util)\
 $(addprefix managers/tests/,$(TESTSOURCES))\
 $(addprefix managers/habits/,$(HABITMANAGERSOURCES))\
 opts/Option Main


all: target/$(JARNAME).jar
	make -C src/main/resources/assistantBotFiles files
	mkdir -p tmp
	rm -rf $(REBOOTFILE)
	#java -jar $< $(KEYS) 2>&1 | tee $(LOGFILE)
	./src/main/pl/run.pl --cmd "java -jar $< $(KEYS) -t $(REBOOTFILE) -c $(RUNCOMMANDSFILE)" $(PERLKEYS) 2>&1 | tee $(LOGFILE)
offline: target/$(JARNAME).jar
	#java -jar $< -o local $(KEYS) 2>&1 | tee $(LOGFILE)
	#java -jar $< -o local $(KEYS) 2>$(LOGFILE)
	mvn exec:exec -Dexec.executable="java" -Dexec.args="-classpath %classpath Main -o local $(KEYS)" 2>$(LOGFILE)

target/$(JARNAME).jar : $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES))) pom.xml
	mvn package
pull:
	git pull
	cd src/main/java/com/github/nailbiter/util && git pull
jar:
	mvn package
gym:
	./src/main/pl/makeGym.pl --program src/main/resources/gym.json
