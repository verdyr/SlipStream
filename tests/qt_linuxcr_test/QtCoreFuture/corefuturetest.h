#ifndef COREFUTURETEST_H
#define COREFUTURETEST_H
#include <QObject>
#include <QString>
#include <QFile>
#include <QTimer>
#include <QFutureWatcher>

class CoreFutureTest: public QObject
{
    Q_OBJECT
public:
    CoreFutureTest(QObject *parent=NULL );
    ~CoreFutureTest();

public slots:
    void startTest();
    void handleFinished();
signals:
    void error();
    void finished();
private:

    QFile m_file;
    int m_count;
    QFutureWatcher<void> m_watcher;
};

#endif // CoreFutureTest_H
