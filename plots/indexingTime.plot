reset
infile ='./data/indexingTime'

outfile="indexingTime"


################ label ################
set key top right
#set title 'Indexing rates'
set xlabel 'Wiki dump year'
set ylabel 'Indexing time in minutes'



################ plot ################
set sample 1000
set yrange [0:100]
set xrange [2003.5:2011.5]
#set logscale y

set term epslatex standalone color solid 8 size 8.5 cm, 7 cm
set out sprintf('%s.tex',outfile)

plot 	infile using ($1):($4):(0.5) with boxes title "GRAPHITY"  ls 1 lw 2 fs pattern 2 , \
	infile using ($1):($3):(0.5) with boxes title "STOU" 	ls 3 lw 2 fs pattern 4
	
 	


#infile using ($1):($3/1000):(0.5) with boxes title "STOU" 	ls 5 lw 4 fs solid border -1
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



