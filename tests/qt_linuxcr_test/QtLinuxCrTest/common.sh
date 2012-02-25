#!/bin/sh

verify_freezer()
{
	line=`grep freezer /proc/mounts`
	if [ $? -ne 0 ]; then
		echo "please mount freezer cgroup"
		echo "  mkdir /cgroup"
		echo "  mount -t cgroup -o freezer freezer /cgroup"
		exit 1
	fi
	freezermountpoint=`echo $line | awk '{ print $2 }'`
	freezerdir=`mktemp -p $freezermountpoint -d` || \
		(echo "mktemp for freezer failed"; exit 1)
	export freezerdir="$freezerdir"
}

verify_paths()
{
	which checkpoint > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "BROK: checkpoint not in path"
		exit 1
	fi
	which restart > /dev/null 2>&1
	if [ $? -ne 0 ]; then
		echo "BROK: restart not in path"
		exit 1
	fi
}
verify_freezer
verify_paths

freeze()
{
	if [ -z "$freezerdir" ]; then
		$freezerdir=${freezermountpoint}/1
	fi
	d=$freezerdir
	echo FROZEN > $d/freezer.state
	while [ `cat $d/freezer.state` != "FROZEN" ]; do
		echo FROZEN > $d/freezer.state
	done
}

freeze_pid()
{
	if [ -z "$freezerdir" ]; then
		$freezerdir=${freezermountpoint}/1
	fi
	if [ ! -d $freezerdir ]; then
		# release agent may have nuked it
		mkdir -p $freezerdir
		while [ ! -d $freezerdir ]; do : ; done
	fi
	echo $1 > $freezerdir/tasks
	cat $freezerdir/tasks > /dev/null  # make sure it updated
	freeze
}

thaw()
{
	if [ -z "$freezerdir" ]; then
		$freezerdir=${freezermountpoint}/1
	fi
	d=$freezerdir
	echo THAWED > $d/freezer.state
	cat $d/freezer.state > /dev/null
}

get_ltp_user()
{
	awk -F: '{ print $1 }' /etc/passwd | grep "\<ltp\>"
	if [ $? -ne 0 ]; then
		echo "I refuse to mess with your password file"
		echo "please create a user named ltp"
		uid=-1
	else
		uid=`grep "\<ltp\>" /etc/passwd | awk -F: '{ print $3 }'`
	fi
}

handlesigusr1()
{
	echo "FAIL: timed out"
	exit 1
}

trap handlesigusr1 SIGUSR1 
timerpid=0

canceltimer()
{
	if [ $timerpid -ne 0 ]; then
		kill -9 $timerpid > /dev/null 2>&1
	fi
}

settimer()
{
	(sleep $1; kill -s USR1 $$) &
	timerpid=`jobs -p | tail -1`
}

CHECKPOINT=`which checkpoint`
if [ $? -ne 0 ]; then
	echo "BROK: checkpoint not found in your path"
	exit 1
fi
RESTART=`which restart`
if [ $? -ne 0 ]; then
	echo "BROK: restart not found in your path"
	exit 1
fi
NSEXEC=`which nsexec`
if [ $? -ne 0 ]; then
	echo "BROK: nsexec not found in your path"
	exit 1
fi
