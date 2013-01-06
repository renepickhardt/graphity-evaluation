reset
stou ='./data/wiki-blou-simulation
graphity ='./data/wiki-graphity-simulation

outfile="wiki-simulation-adds"

################ label ################
set key top right
#set title 'GRAPHITY over Stou ratio'
set xlabel 'Wiki dump year'
set ylabel 'Added follow edges per second'


################ plot ################
set sample 1000
set yrange[1:500]
set xrange [2003.5:2008.5]

set logscale y

set format y '%2.0fk'

set term epslatex standalone color solid 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)

plot 	graphity using ($1-0.1):($4):(0.3) with boxes title "GRAPHITY" ls 1 lw 2 fs pattern 2 , \
	stou using ($1+0.1):($4):(0.3) with boxes title "STOU" ls 3 lw 2 fs pattern 4



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



