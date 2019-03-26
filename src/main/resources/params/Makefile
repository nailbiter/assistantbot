.PHONY: all gym
UPLOADJSONPL=../../pl/uploadJson.pl

FLAGDIR=flags
HABITS=habits mishpuha_habits
PARAMS= alex.params mishpuha_params
FLAGS=userRecords $(HABITS) $(PARAMS)

all: $(addprefix $(FLAGDIR)/,$(addsuffix .txt, $(FLAGS)))

%.json:%.js
	node $< > $@
$(FLAGDIR)/mishpuha_habits.txt: mishpuha_habits.json $(UPLOADJSONPL)
	cat $<|./$(UPLOADJSONPL) --colname mishpuha.habits --dbname logistics --flagfile $@
$(FLAGDIR)/userRecords.txt: userRecords.json $(UPLOADJSONPL)
	cat $< | ./$(UPLOADJSONPL) --dbname logistics --colname '_users' --field 'name' --flagfile $@
$(FLAGDIR)/mishpuha_params.txt: mishpuha_params.json $(UPLOADJSONPL)
	cat $< | ./$(UPLOADJSONPL) --dbname logistics --colname 'mishpuha.params' --flagfile $@
$(FLAGDIR)/alex.params.txt: alex.params.json $(UPLOADJSONPL)
	cat $< | ./$(UPLOADJSONPL) --dbname logistics --colname 'alex.params' --field 'name' --flagfile $@
$(FLAGDIR)/habits.txt: habits.json $(UPLOADJSONPL)
	cat $<|./$(UPLOADJSONPL) --colname alex.habits --dbname logistics --flagfile $@

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
habits.json: merged.pl
	perl $< > $@
mishpuha_habits.json: merged2.pl
	perl $< > $@
merged2.pl: habits2.pl macro.pl
	cat macro.pl > $@
	cat $< >> $@
merged.pl: habits.pl macro.pl
	cat macro.pl > $@
	cat $< >> $@
