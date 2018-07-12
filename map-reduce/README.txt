If running on Cloud Lab:
  cp DeployPom.xml  sc.mapreduce/pom.xml 

cd  sc.mapreduce
mvn clean package

If needed, modify scMapReduce.conf:
  vim  sc.mapreduce/configuration/scMapReduce.conf

cp -r sc.mapreduce/configuration sc.mapreduce/target/

To run:
  cd sc.mapreduce/target/
  $SPARK_HOME/bin/spark-submit  --class edu.umkc.sce.csee.dbis.Main  sc.mapreduce-0.0.1-SNAPSHOT-jar-with-dependencies.jar  --executor-cores 8 --deploy-mode cluster  
