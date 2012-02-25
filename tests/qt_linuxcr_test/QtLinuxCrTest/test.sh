#!/bin/sh

echo $$ > /cgroup/1/tasks
exec 0>&-
exec 1>&-
exec 2>&-
./QtJob.sh
