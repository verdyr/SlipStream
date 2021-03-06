#include <QtCore/QCoreApplication>
#include "corefuturetest.h"
int main(int argc, char *argv[])
{
    QCoreApplication a(argc, argv);

    CoreFutureTest *test = new CoreFutureTest( &a );

    QObject::connect(test, SIGNAL(error()), &a, SLOT(quit()) );
    QObject::connect(test, SIGNAL(finished()), &a, SLOT(quit()) );

    return a.exec();
}
