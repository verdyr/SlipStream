// fib.cpp - Examples of using computational closures.
//
// Project SlipStream, Ubiquitous Architectures Team
// Nokia Research Center, Helsinki
//
// Copyright (c) Nokia Corporation, 2010. All Rights Reserved.
//
// JPL 14.10.2010

#include <tr1/functional>
#include <iostream>
#include <QtCore/QCoreApplication>
#include <QObject>
#include <QDataStream>
#include <QtConcurrentMap>
#include <QFuture>

#include "QBlock.h"

using namespace std;
using namespace std::tr1::placeholders;

class Fib : public QBlock<int>
{
    Q_BLOCK
    Q_CAPABILITIES(Q_PREFER_REMOTE)
    Q_CAPTURE(int m_skip)
    Q_STATE(int m_prev1)
    Q_STATE(int m_prev2)

protected:

    int m_skip;
    int m_prev1;
    int m_prev2;

public:
    Fib(int skip = 0) :
            QBlock( bind ( tr1::mem_fn(&Fib::next), this )  )
    {
        m_skip = skip;
        m_prev1 = 0;
        m_prev2 = 0;

        // Skip the first n elements

        for (int i = 0; i < m_skip; ++i) {
            next();
        }
    }
    int next()
    {
        int rv;

        if ( m_prev1 == 0 ) {
            m_prev1 = 1;
            rv = 1;
        }
        else {
            rv = m_prev1 + m_prev2;
            m_prev2 = m_prev1;
            m_prev1 = rv;
        }

        return rv;
    }

    // Implementation of serialize() and deserialize() is just a mock-up,
    // these mechanisms are under investigation

    bool serialize(QDataStream& outStream)
    {
        outStream << (qint32) m_prev1 << (qint32) m_prev2 << (qint32) m_skip;

        return true;
    }
    bool deserialize (QDataStream& inStream)
    {
        qint32 prev1, prev2, skip;

        inStream >> prev1 >> prev2 >> skip;

        m_prev1 = prev1;
        m_prev2 = prev2;
        m_skip = skip;

        return true;
    }
};

//////////////////////////////////////////////////////////////////////////
// main
//////////////////////////////////////////////////////////////////////////

int main(int argc, char *argv[])
{

    /////////////////////////////////////////////////////////////////////
    // Ex 1: Fibonacci series using a lambda function
    //
    // We declare variable f1 of function type.
    // The function returns an integer and takes no parameters.
    // The lambda expression syntax
    //
    //    [ captured_variables ] ( function_arguments ) { function_body }
    //
    // is the C++0x standard proposal way of defining a closure object, i.e.
    // a functor that may capture variables from its surrounding scope.
    /////////////////////////////////////////////////////////////////////

    cout << "Ex1: ";

    tr1::function<int()> f1;
    tr1::function<QFuture<int>()> f2;
    int skip = 0;

    f1 = [ skip ] () {
        static int prev1 = 0;
        static int prev2 = 0;
        static int i = skip;
        int rv;

        do {
          if ( prev1 == 0 ) {
              prev1 = 1;
              rv = 1;
          }
          else {
              rv = prev1 + prev2;
              prev2 = prev1;
              prev1 = rv;
          }
        } while ( i-- > 0 );

        return rv;
    };

    for (int i=0; i < 10; ++i) {
        cout << f1() << " "; // 1 1 2 3 5 8 13 21 34 55
    }
    cout << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 2: Fibonacci series using QBlock
    /////////////////////////////////////////////////////////////////////

    cout << "Ex2: ";

    f2 = Fib(skip);

    for (int i=0; i < 10; ++i) {
        cout << f2().result() << " "; // 1 1 2 3 5 8 13 21 34 55
    }
    cout << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 3: Fibonacci series using QBlock and a lambda function
    /////////////////////////////////////////////////////////////////////

    cout << "Ex3: ";

    QBlock<int> fibBlock(f1); // RES_T = int, ARG_T = void

    for (int i=0; i < 10; ++i) {
        cout << fibBlock().result() << " "; // 89 144 233 377 610 987 1597 2584 4181 6765
    }
    cout << endl;

    return 0;
}
