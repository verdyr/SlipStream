// Initial draft of the proposed QBLock class, with an example of // a simple counter.
//
// SAB, JPL w/RSB

#include <tr1/functional>
#include <iostream>
#include <QtCore/QCoreApplication>
#include <QObject>
#include <QDataStream>
#include <QtConcurrentMap>
#include <QtConcurrentRun>
#include <QFuture>

#include <QDebug>
#include <QFile>
#define DIRNAME "./sandbox"
#define SHMCREATED DIRNAME "/shm-created"
#define FNAME DIRNAME "/shm-ok"
#define SHMDATA DIRNAME "/shm_data"



using namespace std;
using namespace tr1;
using namespace placeholders;

//////////////////////////////////////////////////////////////////////////
// Macros defined for QBlock derived classes (empty for now) //////////////////////////////////////////////////////////////////////////

#define Q_BLOCK            // For a QBlock derived class
#define Q_CAPABILITIES(x)  // Bitfield of flags
#define Q_STATE(x)         // For each state variable
#define Q_CAPTURE(x)       // For each variable captured by value
#define Q_IO(x)            // For each variable captured by reference

#define Q_PREFER_REMOTE    // Example of a flag in Q_CAPABILITIES

//////////////////////////////////////////////////////////////////////////
// QBlock base class
//////////////////////////////////////////////////////////////////////////

// To do
// - Write a copyable-by-value reference wrapper class that supports shared state // - Provide operator<<() and operator>>() instead of de/serialize() // - Write an example of serializing and deserializing QBlock with state // // Notes // - Q_INVOKABLE only has effect if MOC is extended to support QObject

template<typename RET_T, typename ...ARGS_T> class QBlock {
    typedef RET_T return_type;

public:
    QBlock() { /* ... */ }
    QBlock( const tr1::function<RET_T(ARGS_T...)>& f )
        : m_utilityFunction (f) { /* ... */ }

    Q_INVOKABLE virtual RET_T operator()(ARGS_T...) = 0;
    virtual bool serialize(QDataStream& outStream) = 0;
    virtual bool deserialize(QDataStream& inStream) = 0;

protected:

    tr1::function<RET_T(ARGS_T...)> m_utilityFunction;
};

class Counter : public QBlock<int, int>
{
    Q_BLOCK
    Q_CAPABILITIES(Q_PREFER_REMOTE)
    Q_CAPTURE(int m_initial)
    Q_STATE(int m_count)

protected:

    int m_initial;
    int m_count;

public:
    Counter(int initial) :
        m_initial(initial), m_count(initial)
    {
    }

    Q_INVOKABLE int operator()(int increment)
    {
        //
        int rv = m_count;
        m_count += increment;
        return rv;
    }

    // Implementation of serialize() and deserialize() is just a mock-up,
    // these mechanisms are under investigation

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
        out << "up and running";
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
    for(int j=100; (j < 5000) && (ret == 0); j += 100)
        {
            ret=c1(j);
            usleep(100);
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

