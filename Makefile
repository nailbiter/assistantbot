.PHONY: all

JARNAME=assistantBot-0.0.1-SNAPSHOT-jar-with-dependencies
RESFOLDER=src/main/resources/assistantBotFiles
LOGFILE=log/log.txt
KEYS=-r $(RESFOLDER) -t bottoken -n AlexCovenBot

#sources
UTILSOURCES=
SOURCES=$(addprefix util/,$(UTILSOURCES)) opts/Option Main


all: target/$(JARNAME).jar
	java -jar $< $(KEYS) 2>&1 | tee $(LOGFILE)

target/$(JARNAME).jar : $(addprefix src/main/java/,$(addsuffix .java,$(SOURCES)))
	mvn package
