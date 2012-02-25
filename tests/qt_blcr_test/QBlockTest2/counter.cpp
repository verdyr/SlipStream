// counter.cpp - Examples of using computational closures.
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
#include <QtConcurrentMap>
#include <QtConcurrentRun>
#include <QFuture>

#include "QBlock.h"


#include <QDebug>
#include <QFile>
#define DIRNAME "./sandbox"
#define SHMCREATED DIRNAME "/shm-created"
#define FNAME DIRNAME "/shm-ok"
#define SHMDATA DIRNAME "/shm_data"

using namespace std;
using namespace tr1::placeholders;

class Counter : public QBlock<int, int>
{
    Q_BLOCK
    Q_CAPABILITIES(Q_PREFER_REMOTE)
    Q_CAPTURE(int m_initial)
    Q_STATE(int m_count)

protected:

    int m_initial;
    int m_count;

    int countFunction(int increment)
    {
        int rv = m_count;
        m_count += increment;
        return rv;
    }

public:
    Counter(int initial) :
            QBlock( bind( tr1::mem_fn( &Counter::countFunction ), this, _1 ) )
    {
        m_initial = initial;
        m_count = initial;
    }

    // Implementation of serialize() and deserialize() is
    // under investigation

    bool serialize(QDataStream& outStream)
    {
        outStream << (qint32) m_initial << (qint32) m_count;

        return true;
    }
    bool deserialize (QDataStream& inStream)
    {
        qint32 initial;
        qint32 count;

        inStream >> initial >> count;

        m_initial = initial;
        m_count = count;

        return true;
    }
};

//////////////////////////////////////////////////////////////////////////
// main
//////////////////////////////////////////////////////////////////////////


int main(int argc, char *argv[])
{
    /////////////////////////////////////////////////////////////////////
    // Ex 1: Simple counter using a lambda expression
    //
    // We declare variable c1 of function type.
    // The function takes an integer and returns an integer.
    // The lambda expression syntax
    //
    //    [ captured_variables ] ( function_arguments ) { function_body }
    //
    // is the C++0x standard proposal way of defining a closure object, i.e.
    // a functor that may capture variables from its surrounding scope.
    /////////////////////////////////////////////////////////////////////


    QFile file(QString(SHMCREATED));

    if( file.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
//        if(!file.setPermissions( QFile::WriteUser | QFile::ReadUser | QFile::ExeUser | QFile::ReadGroup | QFile::ExeGroup  | QFile::ReadOther | QFile::ExeOther))
//        {
//            qDebug() << "Could not set permissions";
//        }
        QTextStream out(&file);
        out << "READY";
        file.close();
    }
    else
    {
        qDebug() << "Could not create file: " << QString( SHMCREATED);
    }


    fclose(stdout);
    fclose(stderr);
    fclose(stdin);


    tr1::function<int(int)> c1;
    tr1::function<int(int)> c2;

    QString filename = QString(SHMDATA);

    c1 = [ filename ] ( int max_count) {
        int n=0;
        static QString myFile = filename;
        int n_1 = 1; // n-1=1
        int n_2 = 0; // n-2=0
        QFile file(myFile);
        if( !file.open(QIODevice::WriteOnly | QIODevice::Text | QIODevice::Append) )
        {
            qDebug() << "Could open file: " << file.fileName();
            return -1;
        }
        QTextStream out(&file);
        out << max_count << ":\t";
        out.flush();
        for(int ii=0;  ii < max_count; ii++)
            {
                n = n_1 + n_2;
                n_2 = n_1;
                n_1 = n;
                out << n << "\t";
                out.flush();
            }
        out << endl;
        file.close();

        return 0;
    };

    int ret = 0;
    QBlock<int, int> myBlock(c1); // RET_T = int, ARGS_T = int
    QFuture<int> future;
    for(int j=100; (j < 5000) && (ret == 0); j += 100)
        {
            future = myBlock(j);
            ret = future.result();
        }

    QFile endfile(QString(FNAME));

    if( endfile.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
//        if(!file.setPermissions( QFile::WriteUser | QFile::ReadUser | QFile::ExeUser | QFile::ReadGroup | QFile::ExeGroup  | QFile::ReadOther | QFile::ExeOther))
//        {
//            qDebug() << "Could not set permissions";
//        }
        QTextStream out(&endfile);
        out << "ready";
        endfile.close();
    }
    else
    {
        qDebug() << "Could not create file: " << QString( SHMCREATED);
    }
    return 0;
}


#if 0

int main(int argc, char *argv[])
{
    /////////////////////////////////////////////////////////////////////
    // Ex 1: Simple counter using a lambda expression
    //
    // We declare variable c1 of function type.
    // The function takes an integer and returns an integer.
    // The lambda expression syntax
    //
    //    [ captured_variables ] ( function_arguments ) { function_body }
    //
    // is the C++0x standard proposal way of defining a closure object, i.e.
    // a functor that may capture variables from its surrounding scope.
    /////////////////////////////////////////////////////////////////////

    Q_UNUSED(argc);
    Q_UNUSED(argv);

    cout << "\nEx1: ";

    tr1::function<int(int)> c1;
    tr1::function<QFuture<int>(int)> c2;

    int initial = 1;

    c1 = [ initial ] ( int increment ) {
        static int count = initial;
        int rv = count;
        count += increment;

        return rv;
    };

    for (int i=0; i < 10; ++i) {
        cout << c1(1) << " "; // 1 2 3 ... 10
    }
    cout << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 2: Simple counter using a QBlock
    /////////////////////////////////////////////////////////////////////

    cout << "\nEx2: ";

    c2 = Counter(initial);

    for (int i=0; i < 10; ++i) {
        cout << c2(1).result() << " "; // 1 2 3 ... 10
    }
    cout << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 3: QtConcurrent::mapped() using a lambda expression
    /////////////////////////////////////////////////////////////////////

    cout << "\nEx3: ";

    QList<int> list;
    list << 1 << 1 << 1 << 1 << 1;

    QFuture<int> future = QtConcurrent::mapped ( list, c1 );

    foreach (int i, future.results()) {
        cout << i << " "; // 11 12 13 ... 15
    }
    cout << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 4: QtConcurrent::mapped() using a QBlock
    /////////////////////////////////////////////////////////////////////

    cout << "\nEx4: ";

    future =
            QtConcurrent::mapped (
                    list,
                    tr1::function<int(int)> ( [ &c2 ] ( int x ) { return c2(x).result(); } )
            );

    foreach (int i, future.results()) {
        cout << i << " "; // 11 12 13 ... 15
    }

    cout << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 5: Execute a lambda function using QtConcurrent::run()
    /////////////////////////////////////////////////////////////////////

    cout << "\nEx5: ";

    // Declare a function type and a function variable

    typedef tr1::function<int (int, int)> F;
    F add;

    // In here the compiler should infer that the result type
    // of the lambda function is an int and not complain.

    add = [ ] (int y, int z) { int res; res = y+z; return res; };

    // Now use the Qt mechanism for spawning a thread to calculate
    // results. We have lazy evaluation of the return value via QFuture.

    future = QtConcurrent::run(add,1,2); // result = 3
    cout << future.result() << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 6: Execute a lambda function using QBlock
    /////////////////////////////////////////////////////////////////////

    // Same as previous, using QBlock

    cout << "\nEx6: ";

    QBlock<int, int, int> addBlock(add); // RET_T = int, ARGS_T = int, int

    future = addBlock(1,2);
    cout << future.result() << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 7: QtConcurrent::run() using lambda and bind
    /////////////////////////////////////////////////////////////////////

    cout << "\nEx7: ";

    typedef tr1::function<int (int)> G;

    G incr;

    // We bind the first argument of add to value 1 and
    // the second argument of add will be the first argument
    // of bind. (We could mix the order here...)

    incr = bind(add,1,_1);

    future = QtConcurrent::run(incr,5); // result = 6
    cout << future.result() << endl;

    /////////////////////////////////////////////////////////////////////
    // Ex 8: QtConcurrent::run() using QBlock and bind
    /////////////////////////////////////////////////////////////////////

    cout << "\nEx8: ";

    // Same as previous, using QBlock

    QBlock<int, int> incrBlock(incr); // RET_T = int, ARGS_T = int

    future = incrBlock(5);
    cout << future.result() << endl;
}
#endif
