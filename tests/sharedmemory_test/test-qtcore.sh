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
	killall QtCoreLinuxCrTest
	mkdir sandbox
}

do_checkpoint() {
	settimer 4
	while [ ! -f sandbox/shm-created ]; do : ; done
	canceltimer
	pid=`pidof QtCoreLinuxCrTest`
	if [ "x$pid" == "x" ]; then
		echo "failed to execute testcase"
		exit 2
	fi
	freeze_pid $pid
	${CHECKPOINT} $pid > ckpt.shm
	thaw
	killall QtCoreLinuxCrTest
}

echo "XXX Test 1: simple restart with Qt GUI app"
clean_all
echo "Executing: "$NSEXEC -ci ../QtCoreLinuxCrTest &
$NSEXEC -ci ../QtCoreLinuxCrTest &
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

echo "XXX Test 2: re-create root-owned shm as non-root user"
clean_all
$NSEXEC -ci ../QtCoreLinuxCrTest -u 501 &
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

$NSEXEC -ci ../QtCoreLinuxCrTest -e -u 501 &
do_checkpoint
# restart should be able to create shm
$RESTART --pids --copy-status < ckpt.shm
if [ ! -f sandbox/shm-ok ]; then
	echo "Fail: sysv shm was not re-created"
	exit 1
fi

# can we recreate root ipc objects as non-root user?
clean_all
get_ltp_user
if [ $uid -eq -1 ]; then
	echo "not running ltp-uid test"
	exit 0
fi
$NSEXEC -ci ../QtCoreLinuxCrTest -r -u $uid &
do_checkpoint
chown $uid ckpt.shm
setcap cap_sys_admin+pe $RESTART
cat ckpt.shm | ../../mysu ltp $RESTART --pids --copy-status
setcap -r $RESTART
if [ -f sandbox/shm-ok ]; then
	echo "Fail: uid $uid managed to recreate root-owned shms"
	exit 1
fi
echo "PASS: restart failed as it was supposed to"
