reset
infile ='./data/vary-k-2009'

outfile="vary-k-2009"

################ fit ################
set fit errorvariables

f2(x,A,B)=A/(x*log(x))+B
f(x,A,B)=A/(x)+B

fit f(x,A,B) infile u ($1):($3) via A,B
fit f2(x,A2,B2) infile u ($1):($3) via A2,B2

chi = FIT_WSSR/FIT_NDF

set label sprintf('$\alpha_1 = (%.0f \pm %.0f)kHz$', A/1000, A_err/1000) at graph 0.2, graph 0.8 left
set label sprintf('$\alpha_2 = (%.0f \pm %.0f)kHz$', A2/1000, A2_err/1000) at graph 0.2, graph 0.7 left


################ label ################
set key top right
set key spacing 1.5
#set title '2009 - Warm caches - cache\_type=soft - k=15'
set xlabel 'Stream length k'
set ylabel 'Retrieved streams per second'



################ plot ################
set sample 1000
set yrange [0:70]
#set xrange [1:110]
set format y '%2.0fk'

#set logscale x
#set logscale y

set ytics ("0" 0, \
"10k" 10, \
"20k" 20, \
"30k" 30, \
"40k" 40,\
"50k" 50,\
"60k" 60,\
"70k" 70)


set term epslatex standalone color 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)

plot 	infile using ($1):($3/1000):(2) with boxes title "GRAPHITY"  ls 1 lw 2 fs pattern 2 ,\
 	infile using ($1):($6/1000):(2) with boxes title "STOU" 	ls 3 lw 2 fs pattern 4 ,\
	f(x,A,B)/1000 ls 5  lw 4 lc rgb "black" title '$\frac{\alpha_1}{k}$', \
	f2(x,A2,B2)/1000 ls 3  lw 4 lc rgb "black" title '$\frac{\alpha_2}{k\cdot log(k)}$'

#infile using ($1):($3/1000):(0.5) with boxes title "STOU" 	lt ls 3 lw 3 fs solid border -1
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



