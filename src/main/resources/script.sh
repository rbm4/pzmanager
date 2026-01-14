#!/bin/bash
cd /home/pzuser/pzmanager
git pull
java -Dspring.profiles.active=prod -jar target/pzmanager.jar
