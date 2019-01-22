.PHONY: all gym
UPLOADJSONPL=../../pl/uploadJson.pl

all: userRecords.json alex.params.json
	cat userRecords.json | ./$(UPLOADJSONPL) --dbname logistics --colname '_users' --field 'name'
	cat alex.params.json | ./$(UPLOADJSONPL) --dbname logistics --colname 'alex.params'

%.json:%.js
	node $< > $@

COMMON=\
	commonBenchSquat\
	commonFunctions\
	commonProgramData
ALEXSRC=$(addprefix gym/,getprogram $(addprefix common/,$(COMMON)))
alex.params.json: alex.params.js $(addsuffix .js,$(ALEXSRC))
	node $< > $@
