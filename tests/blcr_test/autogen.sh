#!/bin/sh

set -x
rm -rf autom4te.cache
glib-gettextize --copy --force
libtoolize --automake -f 
#intltoolize --automake --copy --force
aclocal
autoconf --force
autoheader --force
automake --foreign --add-missing --copy --force-missing
