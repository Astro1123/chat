#!/bin/sh
cd `dirname $0`

PROCESS=chatServer.jar

COUNT=$(jps | grep ${PROCESS} | grep -v grep | wc -l)

if [ ${COUNT} -eq 0 ]; then
	java -jar chatServer.jar $1
else
	echo "${PROCESS} is already running."
fi
