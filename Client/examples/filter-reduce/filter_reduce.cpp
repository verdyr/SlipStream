// filter_reduce.cpp - Examples of using QBlock with QtConcurrentFilter library.
//
// SAB, JPL w/RSB 11.11.2010

#include <QtCore/QCoreApplication>
#include <QtConcurrentFilter>
#include <QFuture>
#include <QList>
#include "QBlock.h"
#include <iostream>
#include <tr1/functional>

using namespace std;
using namespace tr1;
using namespace placeholders;

bool startsWith (const QString &testString, const QString start)
{
    return testString.startsWith(start);
}

bool startsWithFoo (const QString &testString)
{
    return testString.startsWith("Foo");
}

int main(int argc, char *argv[])
{
    // constructStartsWithLambda() returns a filter function compatible with Qt Concurrent APIs
    //
    // Note, gcc bug causes a segmentation fault in compilation if the lambda is returned
    // without using a temporary variable (f)

    function<function<bool(const QString&)>(const QString&)> constructStartsWithLambda  =
            [ ] (const QString& start) {
                function<bool(const QString&)> f =
                    [ start ] (const QString& test ) {
                        return test.startsWith(start);
                    };
                return f;
             };

    // StartsWith is a class that defines operator()() and typedef result_type,
    // and thus can be used as a function object for the Qt Concurrent filter function.

    struct StartsWith
    {
        StartsWith(const QString &string)
        : m_string(string) { }

        typedef bool result_type;

        bool operator()(const QString &testString)
        {
            return testString.startsWith(m_string);
        }

        QString m_string;
    };

    struct Reduce
    {

        // Reduce function cannot be a function object in QtConcurrentFilter APIs,
        // thus constructor, operator()() and result_type of class Reduce are not used.

        Reduce() { }

        void operator()(QString& reduced, const QString& intermediate)
        {
            reduced += intermediate + " ";
        }

        typedef void result_type;

        // Reduce function provided as a static method, same as an ordinary C-style function

        static void reduce(QString& reduced, const QString& intermediate)
        {
            reduced += intermediate + " ";
        }
    };

    QList<QString> things;
    things << "Cats" << "Dogs" << "Foos" << "Bars" << "FooBars";

// Lambda (without capture) as a filter - OK

    QFuture<QString> future1 =
            QtConcurrent::filteredReduced( things,
                                           constructStartsWithLambda("Foo"),
                                           Reduce::reduce );

// Function object as a filter - OK

    QFuture<QString> future2 =
            QtConcurrent::filteredReduced( things,
                                           StartsWith("Foo"),
                                           Reduce::reduce );

// Ordinary (static) function as a filter - OK

        QFuture<QString> future3 =
                QtConcurrent::filteredReduced( things,
                                               startsWithFoo,
                                               Reduce::reduce );
// Bound function as a filter - OK

        QFuture<QString> future4 =
                QtConcurrent::filteredReduced( things,
                                               bind (startsWith, _1, QString("Foo")),
                                               Reduce::reduce );

// QBlock based function object as a filter - OK

        function<bool(const QString&)> startsWithFn = constructStartsWithLambda("Foo");

        QBlock<bool, QString> StartsWithBlock =
            QBlock<bool, QString>(startsWithFn);

        QFuture<QString> future5 =
                QtConcurrent::filteredReduced( things,
                                               StartsWithBlock,
                                               Reduce::reduce );

// Function object as a reduce function, does not compile - reduce must be an ordinary function
//
//    Reduce reduceFunctionObject;
//
//    QFuture<QString> future =
//            QtConcurrent::filteredReduced( things,
//                                           StartsWith("Foo"),
//                                           reduceFunctionObject );

    cout << "\nFiltering list 'Cats' << 'Dogs' << 'Foos' << 'Bars' << 'FooBars' using StartsWith('Foo')\n\n";
    cout << "Lambda as a filter:                     " << qPrintable(future1.result()) << endl;
    cout << "Function object as a filter:            " << qPrintable(future2.result()) << endl;
    cout << "Ordinary (static) function as a filter: " << qPrintable(future3.result()) << endl;
    cout << "Bound function as a filter:             " << qPrintable(future4.result()) << endl;
    cout << "QBlock as a filter:                     " << qPrintable(future5.result()) << endl << endl;

    return 0;
}
