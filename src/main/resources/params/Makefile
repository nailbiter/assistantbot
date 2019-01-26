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
GYMSRC=$(addprefix gym/,getprogram $(addprefix common/,$(COMMON)))
SRC=alex.params birthday $(GYMSRC)
alex.params.json: $(addsuffix .js,$(SRC))
	node $< > $@
	
userRecords.json: $(addsuffix .js, userRecords $(addprefix userRecords/, alex))
	node $< > $@
