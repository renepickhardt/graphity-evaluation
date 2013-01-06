reset
infile ='./data/wiki-graphity-simulation

outfile="wiki-graphity-simulation-rates"

################ label ################
set key top right
#set title 'Simulation rates with GRAPHITY on Wiki dumps'
set xlabel 'Wiki dump year'
set ylabel 'Simulated events per second'


################ plot ################
set sample 1000
set yrange[100:500000]
set xrange [2003.5:2008.5]

set logscale y

set term epslatex standalone color solid 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)

plot 	infile using ($1-0.1):($10*1000):(0.3) with boxes title "Remove follow edges" ls 7 lw 2 fs pattern 5 , \
	infile using ($1+0.1):($4*1000):(0.3) with boxes title "Add follow edges" ls 1 lw 2 fs pattern 2 , \
	infile using ($1):($7*1000):(0.3) with boxes title "Create content nodes" ls 3 lw 2 fs pattern 4
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



