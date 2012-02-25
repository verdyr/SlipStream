#-------------------------------------------------
#
# Project created by QtCreator 2010-11-09T15:52:16
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = filter-reduce
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app


SOURCES += \
    filter_reduce.cpp

INCLUDEPATH += ../../include    # svn/SlipStream/qblock/include

HEADERS += ../../include/QBlock.h

QMAKE_CXX = /usr/local/bin/g++  # For the latest version of g++ (at least v4.5)
QMAKE_CXXFLAGS += -std=c++0x    # Enable C++ 0x features such as lambda functions
