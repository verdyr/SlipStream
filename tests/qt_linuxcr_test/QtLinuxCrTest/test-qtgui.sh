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
	killall QtLinuxCrTest
	mkdir sandbox
}

do_checkpoint() {
	settimer 4
	while [ ! -f sandbox/shm-created ]; do : ; done
	canceltimer
	pid=`pidof QtLinuxCrTest`
	if [ "x$pid" == "x" ]; then
		echo "failed to execute testcase"
		exit 2
	fi
	freeze_pid $pid
	${CHECKPOINT} $pid > ckpt.shm
	thaw
	killall QtLinuxCrTest
}

echo "XXX Test 1: simple restart with Qt GUI app"
clean_all
$NSEXEC -ci ../QtLinuxCrTest &
do_checkpoint
# Restart it.  If it finds the shm it created, it creates shm-ok
$RESTART --pids --copy-status < ckpt.shm
if [ ! -f sandbox/shm-ok ]; then
	echo "Fail: sysv shm was not re-created"
	exit 1
fi
echo "PASS"

echo "XXX Test 2: re-create root-owned shm as non-root user"
clean_all
$NSEXEC -ci ../QtLinuxCrTest -u 501 &
do_checkpoint
# restart should fail to create shm
$RESTART --pids --copy-status < ckpt.shm
if [ -f sandbox/shm-ok ]; then
	echo "Fail: sysv shm was re-created"
	exit 1
fi
echo "PASS"

# Create shm as non-root user
echo "XXX Test 3: create shm as non-root user and restart"
clean_all

$NSEXEC -ci ../QtLinuxCrTest -e -u 501 &
do_checkpoint
# restart should be able to create shm
$RESTART --pids --copy-status < ckpt.shm
if [ ! -f sandbox/shm-ok ]; then
	echo "Fail: sysv shm was not re-created"
#	exit 1
fi


