#!/bin/bash
echo Compiling...
mkdir -p bin
find src -name *.java > sources.txt
javac -sourcepath src -classpath lib/*:vertxlibs/* -encoding UTF-8 -d bin @sources.txt
echo Cleaning...
docker-compose down
echo Building images...
docker-compose build --no-cache
docker rmi $(docker images | grep "^<none>" | awk '{print $3}')
docker volume rm $(docker volume ls -qf dangling=true)
echo Starting containers...
docker-compose up -d --force-recreate
#docker-compose logs -f
#killall java
#nohup ./run.sh &
