reset
infile ='./data/strong-cache-1-2-large-degree'

outfile="strong-12-large-degree"

################ label ################
set key top right
#set title 'Warm caches - cache\_type=strong - k=15'
set xlabel 'Wiki dump year'
set ylabel 'Retrieved streams per second'



################ plot ################
set sample 1000
set y2range[*:4.5]
set yrange[0:20]
set xrange [2003.5:2011.5]

set format y '%2.0fk'
set ytics ("0" 0, "5k" 5, \
"10k" 10, \
"15k" 15, \
"20k" 20, \
"25k" 25, \
"30k" 30, \
"35k" 35)



set term epslatex standalone color solid 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)


plot 	infile using ($5-0.1):($3/1000):(0.3) with boxes title "GRAPHITY" 	ls 1 lw 2 fs pattern 2,\
	infile using ($5+0.1):($4/1000):(0.3) with boxes title "STOU" 	ls 3 lw 2 fs pattern 4

#exit


unset out
system sprintf('latex "%s.tex"',outfile)
system sprintf('dvips -o "%s.ps" "%s.dvi"',outfile,outfile)
system sprintf('ps2pdf "%s.ps"',outfile)
system sprintf('rm %s.log',outfile)
system sprintf('rm %s.aux',outfile)
system sprintf('rm %s.ps',outfile)
system sprintf('rm %s.dvi',outfile)
system sprintf('rm %s.tex',outfile)
system sprintf('rm %s-inc.eps',outfile)
system 'rm fit.log'



