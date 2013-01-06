reset
stou ='./data/wiki-blou-simulation
graphity ='./data/wiki-graphity-simulation

outfile="wiki-simulation-updates"

################ label ################
set key top right
#set title 'GRAPHITY over Stou ratio'
set xlabel 'Wiki dump year'
set ylabel 'Created content nodes per second'


################ plot ################
set sample 1000
set yrange[0.1:15]
set xrange [2003.5:2008.5]

set ytics ("100" 0.1, \
"" 0.2, \
"" 0.3, \
"" 0.4, \
"" 0.5, \
"" 0.6, \
"" 0.7, \
"" 0.8, \
"" 0.9, \
"1" 1, \
"" 2, \
"" 3, \
"" 4, \
"" 5, \
"" 6, \
"" 7, \
"" 8, \
"" 9, \
"10k" 10 \
)


set logscale y

set term epslatex standalone color solid 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)

plot 	graphity using ($1-0.1):($7):(0.3) with boxes title "GRAPHITY" ls 1 lw 2 fs pattern 2 , \
	stou using ($1+0.1):($7):(0.3) with boxes title "STOU" ls 3 lw 2 fs pattern 4


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



