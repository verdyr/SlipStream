#include <QtCore/QCoreApplication>
#include "blcrcoretest.h"
int main(int argc, char *argv[])
{
    QString filename("writeTest.txt");
    QCoreApplication a(argc, argv);
    BlcrCoreTest *test = new BlcrCoreTest( filename, &a );

    QObject::connect(test, SIGNAL(error()), &a, SLOT(quit()) );
    QObject::connect(test, SIGNAL(finished()), &a, SLOT(quit()) );
    return a.exec();
}
