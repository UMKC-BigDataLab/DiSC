# compile and package code 
mvn clean compile assembly:single

# create config file
cd ~/DiSC/scripts/gossip
./create-config.sh -n 16 -k 1 -f families-file.txt

# create configuration dir and copy config file
cd ~/DiSC/gossip
mkdir target/configuration
cp  ~/DiSC/scripts/gossip/config.txt target/configuration

# check config file parameters
vi target/configuration/config.txt

# run on a single machine
java -jar gossip-0.1.0-incubating-jar-with-dependencies.jar 
* no cpu restriction
or

taskset -c 05 java -Xmx50g -jar gossip-0.1.0-incubating-jar-with-dependencies.jar
* to use one cpu (core) where 04 is cpu 04

or
taskset -c 04,05 java -Xmx50g -jar gossip-0.1.0-incubating-jar-with-dependencies.jar
* to use two cpus (cores) where 05 is cpu 05 (similar to 04)


# or run on the cluster
#    copy code to home dir
     cp -r target ~/

#    copy code and input files to cluster
     ~/DiSC/scripts/general/cpier.sh target ~/
     ~/DiSC/scripts/general/cpier.sh /dev/data/variables.txt /dev/data/
     ~/DiSC/scripts/general/cpier.sh  /dev/data/families.txt /dev/data/

#    run the code
     ~/DiSC/scripts/general/runClusterCMD.sh 'cd ~/target && taskset -c 00,01,02,03,04,05,06,07 java -Xmx50g -jar gossip-0.1.0-incubating-jar-with-dependencies.jar &> ~/results.example.txt'

