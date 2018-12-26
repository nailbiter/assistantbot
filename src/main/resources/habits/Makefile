.PHONY: all

all: habits.json

habits.json: merged.pl
	perl merged.pl > habits.json
merged.pl: habits.pl macro.pl
	cat macro.pl > merged.pl
	cat habits.pl >> merged.pl
