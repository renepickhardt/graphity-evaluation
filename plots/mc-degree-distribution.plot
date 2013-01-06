reset
infile1='./data/metalcon-degree-distribution'

outfile="mc-degree-distribution"

################ label ################
set key top right
set title 'Node degree distribution'
set xlabel 'Node degree d'
set ylabel 'Number of Nodes'


################ plot ################
set sample 1000
#set yrange[100:*]
#set xrange [0.5:*]

set format y '$10^{%L}$'
set format x '$10^{%L}$'

set logscale xy

set term epslatex standalone color solid 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)

plot 	infile1 using 1:2 title "metalcon.de" lw 1
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



