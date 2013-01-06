reset
infile ='./data/degree-soft-2009'

outfile="degree-soft-2009"

################ fit ################
set fit errorvariables
f(x,A,B)=A/(log(x))+B
f2(x,A,B)=A/(x*log(x))+B
fit f(x,A,B) infile u ($1+5.5):($3) via A,B
fit f2(x,A2,B2) infile u ($1+5.5):($3) via A2,B2

chi = FIT_WSSR/FIT_NDF

set label sprintf('$\alpha = (%.0f \pm %.0f)kHz$', A/1000, A_err/1000) at graph 0.2, graph 0.9 left


################ label ################
set key top right
set key spacing 1.5
#set title '2009 - Warm caches - cache\_type=soft - k=15'
set xlabel 'Average node degree'
set ylabel 'Retrieved streams per second'



################ plot ################
set sample 1000
set yrange [0:16000]
#set xrange [4:16]
#set format y '%2.0fk'

set ytics ("0" 0, "2k" 2000, \
"4k" 4000, \
"6k" 6000, \
"8k" 8000, \
"10k" 10000, \
"12k" 12000, \
"14k" 14000, \
"16k" 16000, \
"18k" 18000, \
"20k" 20000)

set term epslatex standalone color solid 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)

plot 	infile using ($1+5-1):($2):(5) with boxes title "GRAPHITY"  ls 1 lw 2 fs pattern 2 ,\
 	infile using ($1+5+1):($3):(5) with boxes title "STOU" 	ls 3 lw 2 fs pattern 4 ,\
	f2(x,A2,B2) ls 7 lw 4 title '$\frac{\alpha}{d\cdot log(d)}$'
#f(x,A,B) ls 7 lw 4 title '$\frac{A}{log(d)}$', \

#infile using ($1):($3/1000):(0.5) with boxes title "STOU" 	ls 3 lw 3 fs solid border -1
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



