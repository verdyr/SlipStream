// QBlock.h - Initial draft of a class for
// representing computational closures.
//
// Project SlipStream, Ubiquitous Architectures Team
// Nokia Research Center, Helsinki
//
// Copyright (c) Nokia Corporation, 2010. All Rights Reserved.
//
// SAB, JPL w/RSB
//
// Note: to compile, add the following lines to the .pro file in Qt Creator:
//
//   QMAKE_CXX = /usr/local/bin/g++  # For the latest version of g++ (at least v4.5)
//   QMAKE_CXXFLAGS += -std=c++0x    # Enable C++ 0x features such as lambda functions

#ifndef QBLOCK_H
#define QBLOCK_H

#include <QObject>
#include <QDataStream>
#include <QGenericReturnArgument>
#include <QGenericArgument>
#include <QSharedPointer>
#include <QtConcurrentRun>
#include <QFuture>

using namespace std;

//////////////////////////////////////////////////////////////////////////
// Macros defined for use in QBlock derived classes (empty for now)
//////////////////////////////////////////////////////////////////////////

#define Q_BLOCK            // For a QBlock derived class
#define Q_CAPABILITIES(x)  // Bitfield of flags
#define Q_STATE(x)         // For each state variable
#define Q_CAPTURE(x)       // For each read-only variable captured by value
#define Q_IO(x)            // For each read-write variable captured by reference

#define Q_PREFER_REMOTE    // Example of a flag in Q_CAPABILITIES

//////////////////////////////////////////////////////////////////////////
// QBlock base class
//////////////////////////////////////////////////////////////////////////
// To do
// - Rename de/serialize to operator<<() and operator>>()
// - Add a friend class for serializing to/from JSON
// - etc
//////////////////////////////////////////////////////////////////////////

template<typename RET_T, typename ...ARGS_T>
class QBlock
{
    typedef RET_T return_type;
    typedef tr1::function<RET_T(ARGS_T...)> FUNC_T;

protected:

    FUNC_T m_utilityFunction;

    void unpackArguments(QList<QGenericArgument>& list)
    {
        Q_UNUSED(list);
    }

    template<typename ARG2_T, typename ...ARGS2_T>
    void unpackArguments(QList<QGenericArgument>& list, ARG2_T arg, ARGS2_T... args)
    {
        list.append(Q_ARG(ARG2_T, arg));
    }

public:
    QBlock()
        : m_utilityFunction (NULL)
    {
    }

    QBlock( FUNC_T f )
        : m_utilityFunction (f)
    {
    }

    QFuture<RET_T> operator()(ARGS_T... args)
    {
        QList<QGenericArgument> opArgs;
        unpackArguments(opArgs);

        QList<QGenericArgument> runArgs;
        for (int i=0; i < 10; ++i) {
            if (i < opArgs.count() ) {
                runArgs.append(opArgs[i]);
            }
            else {
                runArgs.append(QGenericArgument());
            }
        }

        return ( QtConcurrent::run ( m_utilityFunction, args...) );
    }

    virtual bool serialize(QDataStream& outStream) { Q_UNUSED(outStream); return true; }
    virtual bool deserialize(QDataStream& inStream) { Q_UNUSED(inStream); return true; }
};

#endif // QBLOCK_H
