# Makefile for the report using pdflatex

MAIN := $(patsubst %.tex,%.pdf,$(wildcard *.tex))

%.pdf : %.tex
#	latex $<
#	bibtex $(<:%.tex=%)
	pdflatex $<
	pdflatex $<
	#dvips -Ppdf -G0 -o $(<:%.tex=%.ps) $(<:%.tex=%)
	#ps2pdf $(<:%.tex=%.ps)

all: $(MAIN)
	rm -f *.aux *.log

clean:
	rm -f *.aux *.log *.dvi *.blg *.bbl *.d
	#find plots | grep png | xargs rm -f

realclean:
	rm -f *.aux *.log *.dvi *.blg *.bbl *.d *.ps *.pdf

%.d:	%.tex
	echo "$(@:.d=.pdf) $@: $(@:.d=.tex) \\" > $@
	grep includegraphics lab1.tex | cut -f 2 -d { | cut -f 1 -d } | sed 's/$$/\\/'  >> $@


include	$(MAIN:.pdf=.d)

%.png : %.gpi
	gnuplot -e "set term png; set output '$@'; load '$<'"

.PHONY: all

