#ifndef BLCRCORETEST_H
#define BLCRCORETEST_H
#include <QObject>
#include <QString>
#include <QFile>
#include <QTimer>

class BlcrCoreTest: public QObject
{
    Q_OBJECT
public:
    BlcrCoreTest(QString &filename, QObject *parent=NULL );
    ~BlcrCoreTest();

public slots:
    void writeValues( );

signals:
    void error();
    void finished();
private:

    QFile m_file;
    QTimer *m_timer;
    int m_count;
};

#endif // BLCRCORETEST_H
