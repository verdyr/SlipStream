// writer.cpp - Example of the writer monad.
//
// Project SlipStream, Ubiquitous Architectures Team
// Nokia Research Center, Helsinki
//
// Copyright (c) Nokia Corporation, 2010. All Rights Reserved.
//
// SAB, JPL w/RSB
//
// Based on
// - http://blog.sigfpe.com/2006/08/you-could-have-invented-monads-and.html
//
// See also
// - http://www.haskell.org/all_about_monads/html/writermonad.html
//
// Todo
// - Add example of concatenating debug outputs
// - Change external convenience methods to be static methods of Writer
// - Generalize to work on containers (monoids) other than QString
// - Test to verify that class Writer follows the monadic laws
//   (http://www.haskell.org/haskellwiki/Monad_laws)
// - Create another monadic class and test with Writer
// - Create a base class for monads (based on QBlock?)

#include <tr1/functional>
#include <iostream>
#include <QtCore/QCoreApplication>
#include <QString>

#include "QBlock.h"

using namespace std;
using namespace tr1;
using namespace placeholders;

template <typename VAL_T>
class Writer
{
public:

    typedef function<Writer<VAL_T>(VAL_T)> FUNC_T; // For bind

    ///////////////////////////////////////////////////////////////////////////////////////
    // The Kiesli triple: constructor, unit and bind
    ///////////////////////////////////////////////////////////////////////////////////////

    //  monadic type constructor: m a

    Writer(VAL_T value, const QString& msg) { m_value = value, m_msg = msg; }

    // monadic return: a -> m a

    Writer unit(VAL_T value) { return Writer(value, ""); }

    // monadic bind: m a -> (a -> m b) -> m b

    Writer bind(FUNC_T f) { Writer<VAL_T> w2 = f(m_value); return Writer(w2.m_value, m_msg + w2.m_msg); }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Methods specific to Writer monad
    ///////////////////////////////////////////////////////////////////////////////////////

    const QString& text() { return m_msg; }
    const VAL_T& value() { return m_value; }

protected:

    VAL_T m_value;
    QString m_msg;
};

///////////////////////////////////////////////////////////////////////////////////////////
// Convenience functions
///////////////////////////////////////////////////////////////////////////////////////////

template< typename RET_T, typename ...ARGS_T >
Writer<RET_T> lift ( function< RET_T(ARGS_T...)> f, ARGS_T ...args )
{
    // return Writer<decltype(f)::return_type> ( f (args...), "" );
    return Writer<RET_T> ( f (args...), "" );
}

///////////////////////////////////////////////////////////////////////////////////////////
// Test functions
///////////////////////////////////////////////////////////////////////////////////////////

// plainAdd: x -> y -> z

template<typename T>
T plainAdd (T x, T y)
{
    return x + y;
}

// add: x -> y -> m z

template<typename T>
Writer <T> add(T x, T y)
{
    return Writer<T>(x+y, "add was called. ");
}

///////////////////////////////////////////////////////////////////////////////////////////
// main
///////////////////////////////////////////////////////////////////////////////////////////

int main (int argc, char **argv)
{
    // Ex 1: Add integers using a function that returns a Writer monad

    Writer<int> sum1 = add<int>(1,2);

    cout << "Sum 1= " << sum1.value() << endl; // res = 3

    // Ex 2: Add integers using an ordinary function that is lifted to a Writer monad

    function<int(int,int)> f = plainAdd<int>;

    Writer<int> sum2 = lift<int, int, int>(f, 3, 4); // RES_T = int, ARGS_T = int, int

    cout << "Sum2 = " << sum2.value() << endl; // res = 7

    // Ex 3: Use monadic bind to concatenate three add operations that each return a Writer monad

    typedef function<Writer<int>(int)> FUNC_T;

    FUNC_T incr1 = [](int x){ return Writer<int>(x + 1, "incr1 was called. "); };
    FUNC_T incr3 = [](int x){ return Writer<int>(x + 3, "incr3 was called. "); };
    FUNC_T incr7 = [](int x){ return Writer<int>(x + 7, "incr7 was called. "); };

    Writer<int> sum3 = Writer<int>(1, "").bind(incr1).bind(incr3).bind(incr7);

    cout << "Sum3 = " << sum3.value() << endl; // res = 12
    cout << qPrintable(sum3.text()) << endl;   // res = "incr1 was called. incr3 was called. incr7 was called."

    return 0;
}
