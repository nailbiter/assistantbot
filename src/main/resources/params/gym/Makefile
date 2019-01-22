.PHONY: all gym
UPLOADJSONPL=../../../pl/uploadJson.pl

all: gym.json
	cat $< | ./$(UPLOADJSONPL) --dbname logistics --colname '_users' --field 'name'

COMMON=\
	commonBenchSquat\
	commonFunctions\
	commonProgramData
SOURCES=getprogram $(addprefix common/,$(COMMON))
gym.json: $(addsuffix .js,$(SOURCES))
	node $< > $@
