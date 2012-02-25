#-------------------------------------------------
#
# Project created by QtCreator 2010-09-27T17:54:21
#
#-------------------------------------------------

QT       += core
QT -= gui

TARGET = QBlockTest
TEMPLATE = app

QMAKE_CXX = /opt/local/bin/g++
QMAKE_CXXFLAGS += -std=gnu++0x



INCLUDEPATH +=  /home/hannlain/src/linux-cr/tests-cr/libcrtest

LIBS += -L/home/hannlain/src/linux-cr/tests-cr/libcrtest -lcrtest

SOURCES +=\
    counter4.cpp
