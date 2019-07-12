#!/bin/bash

#pull spark docker image
echo "+-------------------------------------------+"
echo "  Begin Pulling image                        "
echo "+-------------------------------------------+"

#docker pull pavanpkulkarni/spark_image:2.2.1

echo ""
echo "+-------------------------------------------+"
echo "  End Pulling image                          "
echo "+-------------------------------------------+"
echo ""

#create a n node cluster with default configurations
number_of_nodes=3

echo "+-------------------------------------------------+"
echo "Creating %s node cluster " "$number_of_nodes"
echo "+-------------------------------------------------+"

sbt assembly

docker-compose build

#docker-compose up -d --scale slave=$number_of_nodes
#
#echo ""
#echo "+----------------------------------------------------+"
#echo "  %s node cluster  up and running    " "$number_of_nodes"
#echo "  Check status of nodes running 'docker ps -a'        "
#echo "+----------------------------------------------------+"
#echo ""
#
#echo " wait for 10 seconds before executing the Spark Job .........  "
#sleep 10
#
##run the job on the cluster
#echo "+----------------------------------------------------------+"
#echo "  Executing Spark job on %s node cluster " "$number_of_nodes"
#echo "+----------------------------------------------------------+"
#
#docker exec master /opt/spark/bin/spark-submit \
#			--class com.pavanpkulkarni.dockerwordcount.DockerWordCount \
#			--master spark://master:6066 \
#			--deploy-mode cluster \
#			--verbose \
#			/opt/Docker_WordCount_Spark-1.0.jar /opt/sample.txt  /opt/output/wordcount_output
#
#echo ""
#echo "+----------------------------------------------------------------+"
#echo "  Check localhost:8080 for job status                             "
#echo "  Check localhost:18080 for history server                        "
#echo "  Executor port bindings can be found by running 'docker ps -a'   "
#echo "+----------------------------------------------------------------+"
#echo ""

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
