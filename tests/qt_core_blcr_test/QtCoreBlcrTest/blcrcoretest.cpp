#include "blcrcoretest.h"
#include <QTimer>
#include <QCoreApplication>
#include <QDebug>

BlcrCoreTest::BlcrCoreTest(QString &filename, QObject *parent)
    :QObject(parent),m_file(filename), m_count(0)
{
    quint64 pid = QCoreApplication::applicationPid();
    qDebug() << "PID: " << pid;
    m_timer = new QTimer(this);
    connect(m_timer, SIGNAL(timeout()), this, SLOT(writeValues()));
    m_timer->start(10);
}

BlcrCoreTest::~BlcrCoreTest()
{
    if(m_file.isOpen())
        m_file.close();

}

void BlcrCoreTest::writeValues()
{
    if( !m_file.isOpen() && !m_file.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
        qDebug() << "Could open file: " << m_file.fileName();
        emit error();
    }
    else if(m_count == 0)
    {
        qDebug() << "File opened";
    }
    QTextStream out(&m_file);

     m_count++;

     if( (m_count % 80) == 1)
            out << "\n" << m_count;
          else
            out << ".";

      out.flush();

      if(m_count >= 1000)
        emit finished();
}
