#-------------------------------------------------
#
# Project created by QtCreator 2010-08-12T14:36:01
#
#-------------------------------------------------

QT       += core gui

TARGET = QtLinuxCrTest
TEMPLATE = app

INCLUDEPATH +=  /home/hannlain/src/linux-cr/tests-cr/libcrtest

LIBS += -L/home/hannlain/src/linux-cr/tests-cr/libcrtest -lcrtest

SOURCES += main.cpp\
        mainwindow.cpp

HEADERS  += mainwindow.h

FORMS    += mainwindow.ui
