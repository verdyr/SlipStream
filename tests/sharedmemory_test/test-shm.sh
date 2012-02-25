#!/bin/sh
# Copyright 2009 IBM Corp.
# Author: Serge Hallyn

source ./common.sh

dir=`mktemp -p . -d -t cr_shm_XXXXXXX` || (echo "mktemp failed"; exit 1)
echo "Using output dir $dir"
chmod go+rx $dir

cd $dir

clean_all() {
	rm -f ckpt.shm
	rm -rf sandbox
	killall shm_client
	mkdir sandbox
}

do_checkpoint() {
	settimer 4
	while [ ! -f sandbox/shm-created ]; do : ; done
	canceltimer
	pid=`pidof shm_client`
	if [ "x$pid" == "x" ]; then
		echo "failed to execute testcase"
		exit 2
	fi
	freeze_pid $pid
	${CHECKPOINT} $pid > ckpt.shm
	thaw
	killall shm_client
}

echo "XXX Test 1: simple restart with shm "
clean_all
echo "Executing: "$NSEXEC -ci ../src/shm_client &
$NSEXEC -ci ../src/shm_client &
do_checkpoint
echo "Checkpoint done"

# Restart it.  If it finds the shm it created, it creates shm-ok
echo "Restarting, executing: "$RESTART --pids --copy-status < ckpt.shm
$RESTART --pids --copy-status < ckpt.shm
if [ ! -f sandbox/shm-ok ]; then
	echo "Fail: sysv shm was not re-created"
	exit 1
fi
echo "PASS"


