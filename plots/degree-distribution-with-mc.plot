reset
infile1='./data/degree-distribution1075590000'
infile2='./data/degree-distribution1107212400'
infile3='./data/degree-distribution1138748400'
infile4='./data/degree-distribution1170284400'
infile5='./data/degree-distribution1201820400'
infile6='./data/degree-distribution1233442800'
infile7='./data/degree-distribution1264978800'
infileMC='./data/metalcon-degree-distribution'

outfile="degree-distribution-with-mc"

################ label ################
set key top right
#set title 'Node degree distribution'
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

plot 	infile1 using 1:2 title "2004" lw 1 ,\
	infile2 using 1:2 title "2005" lw 1 ,\
	infile3 using 1:2 title "2006" lw 1 ,\
	infile4 using 1:2 title "2007" lw 1 ,\
	infile5 using 1:2 title "2008" lw 1 ,\
	infile6 using 1:2 title "2009" lw 1 ,\
	infile7 using 1:2 title "2010" lw 1 ,\
	infileMC using 1:2 title "Metalcon.de"
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



