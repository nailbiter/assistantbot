.PHONY: all
UPLOADJSONPL=../../pl/uploadJson.pl

all: userRecords.json alex.params.json
	cat userRecords.json | ./$(UPLOADJSONPL) --dbname logistics --colname '_users' --field 'name'
	cat alex.params.json | ./$(UPLOADJSONPL) --dbname logistics --colname 'alex.params'

%.json:%.js
	node $< > $@
