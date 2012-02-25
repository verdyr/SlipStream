#-------------------------------------------------
#
# Project created by QtCreator 2010-08-17T11:14:16
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = QtCoreLinuxCrTest
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app

INCLUDEPATH +=  /home/hannlain/src/linux-cr/tests-cr/libcrtest

LIBS += -L/home/hannlain/src/linux-cr/tests-cr/libcrtest -lcrtest

SOURCES += main.cpp \
    blcrcoretest.cpp

HEADERS += \
    blcrcoretest.h
