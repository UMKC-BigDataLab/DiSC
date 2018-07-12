#!/bin/bash

exit 0

files="0.001-samples 0.005-samples  0.01-samples 0.02-samples 0.04-samples 0.06-samples 0.08-samples 0.10-samples"

for f in $files; do


# untar, unzip
pigz -dc $f".tgz" | tar xf -

# upload
ls -1 $f/ | grep table | xargs -I X hdfs dfs -copyFromLocal $f/X /input/

# remove
rm -rf $f

done;

echo "Done"


