pdftops -eps strong-12.pdf
cp -a strong-12.eps ../../../unisvn/2011/sigmodgraphity/ReadStreamsSizeAllNodes.eps
convert -density 170 strong-12.eps ReadStreamsSizeAllNodes.png

pdftops -eps strong-12-large-degree.pdf 
cp -a strong-12-large-degree.eps ../../../unisvn/2011/sigmodgraphity/ReadStreamsSize.eps
convert -density 170 strong-12-large-degree.eps ReadStreamsSize.png

pdftops -eps degree-distribution-with-mc.pdf
cp -a degree-distribution-with-mc.eps ../../../unisvn/2011/sigmodgraphity/degree-distribution.eps
convert -density 170 degree-distribution-with-mc.pdf degree-distribution.png

pdftops -eps degree-soft-2009.pdf
cp -a degree-soft-2009.eps ../../../unisvn/2011/sigmodgraphity/degree.eps
convert -density 170 degree-soft-2009.pdf degree.png

pdftops -eps vary-k-2009.pdf
cp -a vary-k-2009.eps ../../../unisvn/2011/sigmodgraphity/varyk.eps
convert -density 170 vary-k-2009.pdf varyk.png

pdftops -eps wiki-simulation-updates.pdf 
cp -a wiki-simulation-updates.eps ../../../unisvn/2011/sigmodgraphity/IndexUpdatesCreateContentNode.eps
convert -density 170 wiki-simulation-updates.pdf IndexUpdatesCreateContentNode.png

pdftops -eps wiki-simulation-removes.pdf 
cp -a wiki-simulation-removes.eps ../../../unisvn/2011/sigmodgraphity/IndexUpdatesRemoveFollow.eps
convert -density 170 wiki-simulation-removes.pdf IndexUpdatesRemoveFollow.png

pdftops -eps wiki-simulation-adds.pdf 
cp -a wiki-simulation-adds.eps ../../../unisvn/2011/sigmodgraphity/IndexUpdatesAddFollow.eps
convert -density 170 wiki-simulation-adds.pdf IndexUpdatesAddFollow.png

pdftops -eps indexingTime.pdf 
cp -a indexingTime.eps ../../../unisvn/2011/sigmodgraphity/buildIndexTime.eps
convert -density 170 indexingTime.pdf buildIndexTime.png

mv *.png pngs/

