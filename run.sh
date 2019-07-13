#!/bin/bash

# Create n node cluster with default configurations
numberOfNodes=3

echo "+-------------------------------------------------+"
echo "Creating $numberOfNodes node cluster "
echo "+-------------------------------------------------+"

sbt clean assembly

docker-compose build

docker-compose up -d --scale slave=${numberOfNodes}

echo ""
echo "+----------------------------------------------------+"
echo "  $numberOfNodes node cluster up and running    "
echo "  Check status of nodes running 'docker ps -a'        "
echo "+----------------------------------------------------+"
echo ""

echo " Wait for 10 seconds before executing the Spark Job .........  "
sleep 10

# Run the job on the cluster
echo "+----------------------------------------------------------+"
echo "  Executing Spark job on $numberOfNodes node cluster "
echo "+----------------------------------------------------------+"

docker exec master /opt/spark/bin/spark-submit \
  --class com.backwards.spark.WordCount \
  --master spark://master:6066 \
  --deploy-mode cluster \
  --verbose \
  /opt/spark-backwards.jar --input /opt/sample.txt --output /opt/output

echo ""
echo "+----------------------------------------------------------------+"
echo "  Check localhost:8080 for job status                             "
echo "  Check localhost:18080 for history server                        "
echo "  Executor port bindings can be found by running 'docker ps -a'   "
echo "+----------------------------------------------------------------+"
echo ""

#Uncomment below lines to add logic to bring down the cluster after spark job finish

# submissionId=$(grep submissionId $LogFile | cut -d \" -f4)

# echo "submission Id is : " $submissionId

# driverState=$(curl http://localhost:6066/v1/submissions/status/$submissionId | grep driverState | cut -d \" -f4)

# echo "driver state is : " $driverState

# #remove the cluster iff driver is finished
# if [[ "$driverState" == "FINISH" ]]; then
# 	echo "driver has successfully finished execution.. bringing down the cluster !!! "
# 	docker-compose down
# fi
