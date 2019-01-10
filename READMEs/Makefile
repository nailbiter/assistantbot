.PHONY: all

READMEs=MoneyBot.rus
all: $(addsuffix .html,$(READMEs))

MDFLAGS=-x table
MD=markdown_py
MD=pandoc
MDFLAGS=

%.html: %.md README.head.html
	cat README.head.html > $@
	$(MD) $(MDFLAGS) $< >> $@
