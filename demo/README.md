# DiSC Demo

## DiSC

Distributed Score Computation (DiSC) is a scalable approach for fast, approximate score computation to learn multinomial Bayesian networks over distributed data

## Steps to set up the experiment.
1) Create an experiment on Cloudlab

      Experiements -> Start Experiment

2) Select Profile : ```n-ubuntu-16-nodes```

      Select Number of nodes.
      
      Enter Name and cluster.
      
      Enter Experiemnt Duration.

3) On your local machine, get the public ips of the machines in the cluster. Save them to cluster-machines.txt. with the master machine on the first line and execute the following.

    ```git clone https://github.com/anask/ClusterConfig && cd ClusterConfig```

    ```./set-cluster-passwd.sh cluster-machines.txt arung ~/.ssh/id_rsa 1```
  
    ```./run-instance-setup.sh -a cluster-machines.txt -k ~/.ssh/id_rsa -u arung -p passwordGeneratedAbove```

4) On the master node, clone the DiSC Source Code (https://github.com/anask/DiSC_SRC.git)
 
    ```git clone https://github.com/anask/DiSC_SRC.git```
    
5) On all nodes of the cluster, place the data, variables and families to /dev/data.
    
    ```wget http://vortex.sce.umkc.edu:8080/DiSC/datasets/HIGGS.1of16.tgz```

    ```cp ~/DiSC_SRC/data/HIGGS-variables.txt .```

    ```cp ~/DiSC_SRC/data/HIGGS-families.txt .```
    
    ```scp HIGGS.1of16.tgz HIGGS-variables.txt HIGGS-families.txt arung@ms0938.utah.cloudlab.us:/dev/data ```

 6) Untar the data set and copy them to the slave nodes.
      
      ```tar -xvf HIGGS.1of16.tgz```
      
      ```mv /dev/data/HIGGS.table.txt /dev/data/HIGGS.table.txt.1-0``` (On master)

      ```mv /dev/data/HIGGS.table.txt /dev/data/HIGGS.table.txt.2-0``` (On slave node 1)

      ```mv /dev/data/HIGGS.table.txt /dev/data/HIGGS.table.txt.3-0``` (On slave node 2)

7) On the master node, compile and package the Gossip Code.

    ```cd /users/arung/DiSC_SRC/gossip```
    
    ```mvn clean compile assembly:single```        
        
8) Copy the target to the home folder.

    ```cp -r target/ ~/```
    
9) Create a config directory inside target.

    ```mkdir ~/target/configuration```
    
10) Copy scripts from general to scripts dir.

    ```cp runExp.higgs.sh ../```
    
11) Edit runExp.higgs.sh to set the appropriate files.

12) Edit create-config.sh to set the server prefixes.

13) Modify cpier.sh and logger.sh to correct the server details.

14) Install pip and datasketch

    ```sudo apt-get install python-pip```
    ```sudo pip install datasketch -U```
    ```sudo apt-get install python-dev```
    ```pip install uhashring```
    ```pip install --upgrade pip```

15) Paste the below in /usr/bin/pip
```
#!/usr/bin/python                                                               
                                                                                
import sys                                                                      
from pip._internal import main as _main                                                         
                                                                                
if __name__ == '__main__':                                                      
    sys.exit(_main())
```
16) Install matplotlib.

    ```sudo pip install matplotlib```

17) Start the experiment.

    ```bash runExp.higgs.sh```
    
18) After successfull execution, check the results.
    
    DiSC_SRC/scripts/run/ contains the results for each run with a timestamp

19) To plot the results
    
    Renamge the folder to DATASETNAME . NUMBER_OF_NODES . rRVAL . kKVAL
    
    eg : ```mv 2018-09-05.10-53 higgs.4.r120.k1```

20) Install gnuplot
    ```sudo apt-get install gnuplot```
    
21) Create a Directory higgs inside run.

    ```mkdir /users/arung/DiSC_SRC/scripts/run/higgs```
    
22) copy higgs.4.r120.k1 to ../run/higgs

23) Execute the script ./omni-plotter.pl

    ```./omni-plotter.pl -i ../run/higgs -o ../output -k 1,2,3 -r 40,80,120 -n 16```

