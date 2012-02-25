#-------------------------------------------------
#
# Project created by QtCreator 2010-10-18T14:44:45
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = writer
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app


SOURCES += \
    writer.cpp

HEADERS += \
    ../../../include/QBlock.h

INCLUDEPATH += ../../include    # svn/SlipStream/qblock/include

QMAKE_CXX = /usr/local/bin/g++  # For the latest version of g++ (at least v4.5)
QMAKE_CXXFLAGS += -std=c++0x    # Enable C++ 0x features such as lambda functions
