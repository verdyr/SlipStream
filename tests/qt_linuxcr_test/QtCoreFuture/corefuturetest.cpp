#include "corefuturetest.h"
#include <QTimer>
#include <QCoreApplication>
#include <QDebug>
#include <QFile>
#include <QFuture>
#include <QThread>
#include <QtConcurrentRun>
#include <math.h>

#define DIRNAME "./sandbox"
#define SHMCREATED DIRNAME "/shm-created"
#define FNAME DIRNAME "/shm-ok"
extern "C" {
#include <libcrtest.h>
}

CoreFutureTest::CoreFutureTest(QObject *parent)
    :QObject(parent)
{

    if (!move_to_cgroup("freezer", "1", getpid())) {
        printf("Failed to move myself to cgroup /1\n");
        exit(1);
    }
    QTimer::singleShot(0,this, SLOT(startTest()));
}

CoreFutureTest::~CoreFutureTest()
{


}

void writeToFile()
{
    quint64 pid = QCoreApplication::applicationPid();
    QFile createdFile(QString(SHMCREATED));
    int count = 0;
    if( createdFile.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
        //        if(!file.setPermissions( QFile::WriteUser | QFile::ReadUser | QFile::ExeUser | QFile::ReadGroup | QFile::ExeGroup  | QFile::ReadOther | QFile::ExeOther))
        //        {
        //            qDebug() << "Could not set permissions";
        //        }
        QTextStream out(&createdFile);
        out << pid;
        createdFile.close();
    }
    else
    {
        qDebug() << "Could not create file: " << QString( SHMCREATED);
        return;
    }

//    QFile file( DIRNAME "/WriteTest.txt");

//    if( !file.open(QIODevice::WriteOnly | QIODevice::Text) )
//    {
//        qDebug() << "Could open file: " << file.fileName();
//        return;
//    }

//    QTextStream out3( &file);

    while( count < 500)
    {
        count++;

//        if( ( count % 80) == 1)
//            out3 << "\n" << count;
//        else
//            out3 << ".";

//        out3.flush();
        int i= 100000;
        double d=0;
        double c = 0;
        while(i>0)
            {
                i--;
                c= pow(i,2.0);
                d= c+d;
            }

    }
 //   file.close();
    QFile readyFile(QString(FNAME));

    if( readyFile.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
        //        if(!file.setPermissions( QFile::WriteUser | QFile::ReadUser | QFile::ExeUser | QFile::ReadGroup | QFile::ExeGroup  | QFile::ReadOther | QFile::ExeOther))
        //        {
        //            qDebug() << "Could not set permissions";
        //        }
        QTextStream out2(&readyFile);
        out2 << "ready";
        readyFile.close();
    }
    else
    {
        qDebug() << "Could not create file: " << QString( FNAME );
    }
}

void CoreFutureTest::startTest()
{
#if 1
    QObject::connect(&m_watcher, SIGNAL(finished()), this, SLOT(handleFinished()));

    QFuture<void> future = QtConcurrent::run( writeToFile );
    m_watcher.setFuture(future);
#else
    writeToFile();
    emit finished();
#endif
}

void CoreFutureTest::handleFinished()
{
    emit finished();
}
