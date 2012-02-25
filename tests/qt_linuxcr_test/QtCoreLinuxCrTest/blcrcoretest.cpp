#include "blcrcoretest.h"
#include <QTimer>
#include <QCoreApplication>
#include <QDebug>
#include <QFile>
#define DIRNAME "./sandbox"
#define SHMCREATED DIRNAME "/shm-created"
#define FNAME DIRNAME "/shm-ok"
extern "C" {
#include <libcrtest.h>
}

BlcrCoreTest::BlcrCoreTest(QString &filename, QObject *parent)
    :QObject(parent),m_file(filename), m_count(0)
{
    quint64 pid = QCoreApplication::applicationPid();
    if (!move_to_cgroup("freezer", "1", getpid())) {
            printf("Failed to move myself to cgroup /1\n");
            exit(1);
    }


    QFile file(QString(SHMCREATED));

    if( file.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
//        if(!file.setPermissions( QFile::WriteUser | QFile::ReadUser | QFile::ExeUser | QFile::ReadGroup | QFile::ExeGroup  | QFile::ReadOther | QFile::ExeOther))
//        {
//            qDebug() << "Could not set permissions";
//        }
        QTextStream out(&file);
        out << pid;
        file.close();
    }
    else
    {
        qDebug() << "Could not create file: " << QString( SHMCREATED);
    }
    m_timer = new QTimer(this);
    connect(m_timer, SIGNAL(timeout()), this, SLOT(writeValues()));
    m_timer->start(10);

    fclose(stdout);
    fclose(stderr);
    fclose(stdin);

}

BlcrCoreTest::~BlcrCoreTest()
{
    if(m_file.isOpen())
        m_file.close();

}

void BlcrCoreTest::writeValues()
{
#if 1
    if( !m_file.isOpen() && !m_file.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
        qDebug() << "Could open file: " << m_file.fileName();
        emit error();
    }
    else if(m_count == 0)
    {

    }
    QTextStream out(&m_file);

     m_count++;

     if( (m_count % 80) == 1)
            out << "\n" << m_count;
          else
            out << ".";

      out.flush();
#else
      m_count++;
#endif
      if(m_count >= 100)
      {
          QFile file(QString(FNAME));

          if( file.open(QIODevice::WriteOnly | QIODevice::Text) )
          {
      //        if(!file.setPermissions( QFile::WriteUser | QFile::ReadUser | QFile::ExeUser | QFile::ReadGroup | QFile::ExeGroup  | QFile::ReadOther | QFile::ExeOther))
      //        {
      //            qDebug() << "Could not set permissions";
      //        }
              QTextStream out(&file);
              out << "ready";
              file.close();
          }
          else
          {
              qDebug() << "Could not create file: " << QString( SHMCREATED);
          }
        emit finished();
    }
}
