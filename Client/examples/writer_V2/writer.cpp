// writer.cpp - Example of the Writer monad.
//y
// Project SlipStream, Ubiquitous Architectures Team
// Nokia Research Center, Helsinki
//
// Copyright (c) Nokia Corporation, 2010. All Rights Reserved.
//
// SAB, JPL w/RSB  23.11.2010,27.05.2011
//
// ======================= THIS VERSION
// This version abondons development of Writer further (its incomplete, but working). Instead, Monad,
// with fixed underlying (int) and monad (QString) variable types.
// The monad laws are tested, which are examples. Also an example logger (degugger) of the test functions is
// given.
// ============================ skip on to Monad, to be updated after review.
// Based on
// - http://blog.sigfpe.com/2006/08/you-could-have-invented-monads-and.html
//
// See also
// - http://www.haskell.org/all_about_monads/html/writermonad.html
// - http://en.wikipedia.org/wiki/Monad_(functional_programming)
//   important quote from above: "a monad is a kind of abstract data type constructor used to represent computations"
//
//   Thus, more or less: A monad is ABOUT data, manipulated by functions (computations) which are themselves represented
//   and manipulated as data.


//
// Todo
// - more references (e.g. http://net.pku.edu.cn/~course/cs501/2008/resource/haskell/fp_course/lecture-12.pdf
// . fix Write::lift //lift now done in example using Write::unit
// . build variable passing in a chain (>>=) should keep the variables within the closure of the chain (see "prog")
//        this may be done with a hash table.
// - Verify that class Writer follows the monadic laws
// . use QVariant to simulate lazy binding of type variables to type values
// - fix show() in M_result for other bindings of VAL_T and EXT_T
//   (http://www.haskell.org/haskellwiki/Monad_laws)
// - Create another monadic class and test with Writer
// - Create a base class for monads

#include <tr1/functional>
#include <iostream>
#include <QtCore/QCoreApplication>
#include <QString>
#include <QHash>
#include <QDebug>

using namespace std;
using namespace tr1;
using namespace placeholders;

//#include "QBlock.h"
//to do: wrap Monad<-functions with QBlock

#define FUNC_T(VAL_T,EXT_T) function<M_result<VAL_T, EXT_T>(VAL_T)>

template <typename VAL_T, typename EXT_T>
class M_result
// This class is a data container capturing/wrapping the pair of function output data and output data side effect (note
// signular) into a single data output (the "container"), making the function output "pure". That is, a function with one
// computational result. A function which outputs such a container is suitable for use in monad transformations, which in
// SlipStream (perhaps wider) parleance is to say, "executing a monad chain". Such a function

// in Haskell,  newtype Dt s a = DtC (\s -> (s, a))
// in C++: class Dt { Dt(tr1::function<M_result(s)> f&) }
{
    //  monadic container type: m a
    // ?? typedef function<M_result<VAL_T, EXT_T>(VAL_T)> FUNC_T; // For monad construction

    public:
    M_result () {} // this is needed because ...???????
    M_result (VAL_T val_in, EXT_T ext_in)
        : val(val_in), ext(ext_in){}

    VAL_T val;
    EXT_T ext;

    QString show()
    {// this is not general - it is specific to VAL_T bound to int and EXT_T bound to QString
        return "(" + QString::number(val) + ", \"" + ext + "\")" ;
    }
};

template <typename VAL_T, typename EXT_T>
class Writer
{
public:

    ///////////////////////////////////////////////////////////////////////////////////////
    // basic monad requirements: constructor, lift, bind and return
    ///////////////////////////////////////////////////////////////////////////////////////

    Writer(){}

//    Writer(FUNC_T(VAL_T,EXT_T) f_init)
    Writer(function<M_result<VAL_T, EXT_T>(VAL_T)> cf)
    {
        //This is an constructed with an externally supplied function.
        //Thus, we must lift it to the general internal form, which carries the variable referencing environment (hash table) thru a chain of monads being run.
        f = [cf](VAL_T x, QHash<QString,EXT_T>& varRef){ return cf(x); };
        //  -- where the hash table is for variable referencing, as needed, though not needed for this instance.
    } // cf :: VAL_T -> (VAL_T, EXT_T) // Writer $ cf
    // the above represents the concept of constructing a monad, wrapping a function of type FUNC_T

    // monadic lift
    //unit x = (x,"")
    //lift f = unit . f
    static M_result<VAL_T,EXT_T> unit(VAL_T value) { return M_result<VAL_T, EXT_T>(value, ""); } //so, were thinking: "" binds EXT_T
    static function<M_result<VAL_T,EXT_T>(VAL_T)> lift (function<VAL_T(VAL_T)> f) {
        return (function<M_result<VAL_T,EXT_T>(VAL_T)>)([f](int x){ Writer<VAL_T,EXT_T>::unit(f(x)); }); };

    // monadic (>>):: m a -> m b -> m b
    // implemention: bind (next_m) same as above, but nothing "returned" to the surrounding scope.

//    Writer<VAL_T,EXT_T> bind (Writer<VAL_T,EXT_T> &nextM, function<EXT_T()> *extF_p=NULL)
            //The second parameter, when defined, implements an inadeqate scheme for passing the 2nd element of a W_result pair through an running chain
            //of WriterT,as it prevents the chain from being constructed and run in separate closures. I.e. it works here, but is based on imperative,
            // not functional thinking - sorry, it takes time to get it :-0
            //
            //This relates to the to-do entry: "building variable passing in a chain (>>=) "...
            //The suggestion to use a hash table also falls to the imperative camp, although it would work by correctly addressing the need
            //to define a variable name in one closure (bind) but to define its value in another (run)
            //
            //so, as they say in Haskell, "Read my types". Now, below, (>>) is (>>) and (>>=) is (>>=)

    Writer<VAL_T,EXT_T> bind (Writer<VAL_T,EXT_T> nextM) const
    {
        function<M_result<VAL_T,EXT_T>(VAL_T, QHash<QString,EXT_T>&)> fc = [f, nextM](VAL_T x, QHash<QString,EXT_T>& varRef)
        {
            M_result<VAL_T,EXT_T> result = f(x, varRef);
            return nextM.f(result.val, varRef);
        };
        return Writer<VAL_T,EXT_T>(fc);
    }

    // monadic (>>=):: m a -> (a -> m b) -> m b
    // implementation: bind (next_m, &v) generates the monad composition of the left and right monad functions, which includes
    // generating the function which produces (at run time) the EXT_T variable and returning to the surrounding scope (by assigning
    // the function pointer, v
    //
    Writer<VAL_T,EXT_T> bind (EXT_T &par, function<Writer<VAL_T,EXT_T>(function<const EXT_T(QHash<QString,EXT_T>&)>)> nextM_fromAf)
    {
        function<M_result<VAL_T,EXT_T>(VAL_T, QHash<QString,EXT_T>&)> fc = [f, nextM_fromAf, par](VAL_T x, QHash<QString,EXT_T>& varRef)
        {
            M_result<VAL_T,EXT_T> result = f(x,varRef);
            varRef[par] = result.ext; //var now "in scope" for the rest of the chain via the hash table being passed through
            Writer<VAL_T,EXT_T>nextM = nextM_fromAf([result](QHash<QString,EXT_T> &varRef){return result.ext;});
            return nextM.f(result.val, varRef);
        };
        return Writer<VAL_T,EXT_T>(fc);
    }

    // monadic return: a -> m a
    // is a function which takes a parameter of type a, and produces a monad (function) of type m a
    // the monad: return r = C $ \n -> (n,r) -- where the lambda function is the reutrn (unit) function.
    static Writer<VAL_T,EXT_T> const ret (function<const EXT_T(QHash<QString,EXT_T>&)> r)
    {
        function<M_result<VAL_T,EXT_T>(VAL_T,QHash<QString,EXT_T>&)> fc = [r](VAL_T x, QHash<QString,EXT_T>& varRef)
        {
            return M_result<VAL_T,EXT_T> (x,r(varRef));
        };
        return Writer<VAL_T,EXT_T>(fc);
    }
#if 0
    //a second form is needed, since chain building style is: ((link).link)
    //where the var of type a, in a -> m a, is value bound during chain execution in the new monad created by (link)
    //thus (x >> return r) is x.ret(r)  and (x >>= \y -> return r) is x.ret(&y, r);
    Writer<VAL_T,EXT_T> ret (QString &par, function<const EXT_T()> rf) const
    {
        function<M_result<VAL_T,EXT_T>(VAL_T)> fc = [f, par, rf, this](VAL_T x)
        {
            M_result<VAL_T,EXT_T> result = f(x);
            result.ext = rf();
            this->varsNow()[par] = result.ext;
            qDebug() << "result NOW given: " << result.show() << endl;
            return result;
        };

        return Writer<VAL_T,EXT_T>(fc);
    }
#endif
    ///////////////////////////////////////////////////////////////////////////////////////
    // Method to run a monad chain starting with initial value
    ///////////////////////////////////////////////////////////////////////////////////////

    M_result<VAL_T,EXT_T> static run(VAL_T value, Writer<VAL_T,EXT_T> aM) {
        return aM.evaluate(value);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Methods specific to Writer monad
    ///////////////////////////////////////////////////////////////////////////////////////

    const EXT_T& ext_value() { return m_result.ext; }
    const VAL_T& value() { return m_result.val; }

protected:

    function<M_result<VAL_T, EXT_T>(VAL_T, QHash<QString,EXT_T>&)> f;
    M_result<VAL_T, EXT_T> m_result;

    M_result<VAL_T,EXT_T> evaluate (VAL_T x) {
         return f(x, varRef);
    }

    QHash<QString, EXT_T> varRef;
    Writer(function<M_result<VAL_T, EXT_T>(VAL_T, QHash<QString,EXT_T>&)> cf)
    {  f = cf; } // cf :: VAL_T -> (VAL_T, EXT_T) // Writer $ cf -- where the hash table is for variable referencing, as needed.
    // the above represents the concept of constructing a monad, wrapping a function of type FUNC_T
};
//********************************* Monad, a writer monad
class AMonad;

//MFC :: a pure function which is composable with itself regard to the monad underlying type
typedef function<int(int)> McF;
//FofMF :: a function with input of the monad underlying type, production a pair of the monad type and underlying type
typedef function<M_result<int,QString>(int)> FofM;
//MextF :: a function with no formal parameter producing a monad type output.
typedef function<const QString()> MextF;
//MF :: a function which produces a monad, possibly based on value of the monad type.
//Monad::ret is such a function
typedef function<AMonad(MextF &)> MF;
//if needed, a macro for a function producing the given monad, independent of the formal parameter.
//this may be used for (a -> m b), where m b is not really defined by a, but a is an exposed variable for later calculations
#define MFUNCT(m) (MF)([m]( MextF dummy ) { return m; })

// data MonadT int QString = Monad (int->(int,QString))
// i.e. :t Monad is (int->(int,QString)) -> MonadT int QString1
class AMonad
{
public:
//Constructor
    AMonad (FofM mF) { f = mF; };

//unit function
    M_result<int,QString> static unit (int x) { return M_result<int,QString> (x,""); }

//lift function
    FofM static lift (McF liftableF)
    { return [liftableF](int x){ return AMonad::unit(liftableF(x)); }; }

// (>>) :: m a -> m b -> m b
    AMonad bind (AMonad nextM) const
    {
        //we create a closure using a tr1::function, capturing this and the next monad
        function<M_result<int,QString>(int)> fc = [f, nextM](int x)
        {
            M_result<int,QString> result = f(x);
            return nextM.f(result.val);
        };
        return AMonad(fc);
    }

    //
    // (>>=) :: m a -> (a -> m b) -> m b
    //          P1  -> P2         -> RES
    // The first parameter, P1, m a is "this" class instance, so it has no explicit parameters in this method.
    //
    // The second parameter of the bind, P2, is a function of type MF:
    // (a -> m b)
    //  P1' -> RES'
    // This function takes as its parameter P1' of type MextF and
    // produces a monad RES' (which wraps an underlying function which produces the tuple reuslt, M_result).
    //
    // 'par', below, is the reference to the value to be applied to the parameter P1' of the function, P2.
    // It is the ADDRESS of some function of type MextF in the closure where the monad chain is expressed,
    // and thus visible to (in the scope of) the rest of the chain specification.
    // The value is generated when the chain runs and is applied to the formal parameter of P2.
    //
    // 'nextM_fromAf', below, is a P2

    AMonad bind (MextF *par, MF nextM_fromAf) const
    {
        function<M_result<int,QString>(int)> fc = [f, par, nextM_fromAf](int x)
        {
            M_result<int,QString> result = f(x);
            QString parV = result.ext;
            MextF parF = [parV](){ return parV; };
            if(par) *par = parF;
            //AMonad nextM = nextM_fromAf(parV);
            return nextM_fromAf(parF).f(result.val);
        };
        return AMonad(fc);
    }
#if 1 //the method below is thought to be convenient for avoiding an extra wrapper for nextM_fromAf,
      //where other hooked monad values (embedded in mfPar) are relevant to the result of the produced monad
      //and expressed knowing that mfPar will be applied to formal parmeter of nextM_fromAf instead par.
    AMonad bind (MextF *par, MF nextM_fromAf, MextF &mfPar) const
    {
        function<M_result<int,QString>(int)> fc = [f, par, nextM_fromAf, &mfPar](int x)
        {
            M_result<int,QString> result = f(x);
            QString parV = result.ext;
            if (par) *par = [parV](){ return parV; };
            //parV = mfPar();
            //Monad nextM = nextM_fromAf(parV); //copy problem
            AMonad nextM = nextM_fromAf(mfPar);
            return nextM.f(result.val);
        };
        return AMonad(fc);
    }
#endif

    // a convenience match for the case of:   this >>= \par -> nextM
    // short cut of f par = nextM and then this >>= \par -> f par
    // When nextM is already defined, it is just overhead to wrap it up in a f, ignoring par: [nextM](MextF &par){ return nextM; }.
    // i.e. par is only used to hook the manod variable produced by this.
    AMonad bind (MextF *par, AMonad nextM) const
    {
        function<M_result<int,QString>(int)> fc = [f, par, nextM](int x)
        {
            M_result<int,QString> result = f(x);
            QString parV = result.ext;
            if (par) *par = [parV](){ return parV; };
            return nextM.f(result.val);
        };
        return AMonad(fc);
    }

    //
    // 'parF' is a function which produces
    // the value to be used by ret.
    //
    AMonad static ret (MextF &parF)
    {
        function<M_result<int,QString>(int)> fc = [parF](int x)
        {
            M_result<int,QString> result = AMonad::unit(x);
            result.ext = parF();
            return result;
        };
        return AMonad(fc);
    }
#if 1
    // Required when the first monad in a chain is produced by an MF function
    // whose parameter may depend on computation made during the evaluation
    // a convenience, equivalent to Monad(Monad::unit).bind(NULL,
    AMonad static f2m (MF &m_fromAf, MextF &parF)
    {
        function<M_result<int,QString>(int)> fc = [&m_fromAf, &parF](int x)
        {
            return m_fromAf(parF).f(x);
        };
        return AMonad(fc);
    }
#endif
    M_result<int,QString> run (int x) const { return f(x); }

private:
    function<M_result<int, QString>(int)> f;
    M_result<int, QString> m_result;
};

///////////////////////////////////////////////////////////////////////////////////////////
// Test functions
///////////////////////////////////////////////////////////////////////////////////////////

// plainIncr: x -> x

int plainIncr1 (int x)
{
    return x + 1;
}

// add1,dec1 :: x -> (x, y)

M_result<int, QString> add1(int x)
{
    return M_result<int,QString>(x+1, "[add1(" + QString::number(x) + ") was called]");
}

M_result<int, QString> dec1(int x)
{
    return M_result<int,QString>(x-1, "[dec1(" + QString::number(x) + ") was called]");
}

///////////////////////////////////////////////////////////////////////////////////////////
// main
///////////////////////////////////////////////////////////////////////////////////////////

typedef M_result<int, QString> W_result;
typedef function<W_result(int)> W_Func;
typedef Writer<int, QString> WriterT;
typedef function<const QString(QHash<QString,QString>&)> WriterT_extF; // see note with Writer::bind
#define WriterT_string(string) (WriterT_extF)([](QHash<QString,QString>& varRef){ return string; })

#define kF(r,m) = (function<Writer<VAL_T,EXT_T>(EXT_T &)>)([r&](EXT_T rDummy&){ return m; })

// ------------
    FofM bindx (FofM f1, FofM f2)
    {
        return [f1, f2](int x) { return f2(f1(x).val); };
    }

// ------------
int main (int argc, char **argv)
{
    W_Func f; //reuseable SINCE the closures are formed lexically and copied before reassignment

    // ####################################################################################
    // ## trying the unit funtion

    f = WriterT::unit;
    W_result result = f(1);
    cout << "Writer unit (1): " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // ## show a composition with unit - a "lift" of plainIncr1

    function <int(int)> fn = plainIncr1;

    //W_Func liftedIncr1x = WriterT::lift(fn);
// ??????? can't get the above to compile
    f = [f, fn](int x){ return f(fn(x)); };
    result = f(1);
    cout << "Writer lifted plainIncr1(1): " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // ## create inc, dec and lifted monad functions

    WriterT incLiftedM (f);
    f = add1;
    WriterT incM (f);
    f = dec1;
    WriterT decM (f);

    // ####################################################################################
    // ## invoke a monad function, not yet a composition chain (binding) of monads

    result = WriterT::run(2, decM);
    cout << "Writer run(2, decM): " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // ## verify use of WriteT_extF, used to reference variables in a monad chain, specified at chain build time, assigned at run time.

    QHash<QString,QString> ht; //not used, if string contains not variables.
    WriterT_extF txt = [](QHash<QString,QString>& vr){ return "simple "; };
    QString str = txt(ht);
    cout << "WriterT_string(\"simple string\"): " << qPrintable(str) << qPrintable((WriterT_string("string"))(ht)) << endl;

    // ####################################################################################
    // ## invoke monad of return function

    // do { return "string" }
    result = WriterT::run(2, WriterT::ret(WriterT_string("returned string")));
    cout << "Writer run(2, WriterT::ret(WriterT_string(\"returned string\"))) ): " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // ## invoke a monad chain, using (>>) only

    WriterT prog;

    prog = incM.bind(WriterT::ret(WriterT_string("hi")));
    result = WriterT::run(5, prog);
    cout << "Writer run(5, prog): " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // ## invoke a monad chain, using (>>=)

//#define nMF(f) (function<Writer<int,QString>(QString &)>) ([f](QString x){ return f; })
#define nMF(f) (function<Writer<int,QString>(function<const QString(QHash<QString,QString>&)>)>)  ([f](WriterT_extF txt){ return f; })

    //prog = ([incM, decM, incLiftedM]()
    //{
        QString a("a");
        QString b("b");
        QString c("c");
        QString d("d");
        function<Writer<int,QString>(function<const QString(QHash<QString,QString>&)>)> nextM_fromAf;
        nextM_fromAf = [decM](WriterT_extF txt){ return decM; };

        //still to fix:  .....bind(d,WriterT::ret(..));
        //also, ret should NOT run here, as it does... so, function and parameters as separate arguments to bind
        prog = incM.bind(a, nextM_fromAf).bind(b,nMF(incLiftedM)).bind(c,nMF(decM)).bind(WriterT::ret(WriterT_string(varRef["a"]+varRef["b"]+varRef["c"]+varRef["d"]+"--in chain string--")));
        //})(); //not workin yet: creating the program in stack, and copying it out (has work with other versions, but then not variable closure
        result = WriterT::run(4, prog);
        cout << "Writer run(4, prog): " << qPrintable(result.show()) << "\n";
#if 0
        ===================== things below are still to be fixed to new method - then also monadic law confirmaitons
#endif

        //QString b,c,d,  *b_=&b, *c_=&c, *d_=&d;
        //prog = incM.bind(&a,nMF(incM)).bind(&b,nMF(incLiftedM)).bind(&c, nMF(decM)).ret(&d, [a_,b_,c_,d_](){ return (*a_)+(*b_)+(*c_)+(*d_); });

        W_Func z_ = WriterT::unit;
        WriterT z (z_); // fix overload ambiguity   (WriterT::unit);
        //prog = prog;//z.ret("hi");
#if 0
        WriterT *y;
        prog = ([incM](){
            WriterT_extF *v,a;
            const QString (*f)();
            a = [](){ const QString s ("in chain build phase"); return s; };
            //a = f;
            qDebug() << "f at @ " << (*f) << endl;

            return incM.bind(WriterT::ret([](???? use macro ){ return varRef["a"]+"--chain level comment--"; });
            })();
        qDebug() << "a of main @ " << &a << endl;
        y = ([]()
               {
                    WriterT incLiftedM ([](int x){ return WriterT::unit(plainIncr1(x)); });
                    WriterT incM (add1);
                    WriterT decM (dec1);

                    QString a,b,c,d,  *a_=&a, *b_=&b, *c_=&c, *d_=&d;
                    Wext_Func rAccumulation = [a_,b_,c_,d_](){ return (*a_)+(*b_)+(*c_)+(*d_); };

                    WriterT *z,p;
                    z = new WriterT (WriterT::unit);
                    *z = incM.bind(&a,nMF(incM)).bind(&b,nMF(incLiftedM)).bind(&c, nMF(decM)).ret(&d, rAccumulation);

                    W_result result = WriterT::run(4, *z);   //incM.returnM("return text")));
                    cout << "Writer run(4, prog): " << qPrintable(result.show()) << "\n";

                    return z;
               })();
        //prog = *y;
#endif
#if 0
        prog = [incM, decM, incLiftedM](){
            QString a,b,c,d;
            return WriterT ();     //.ret(&a, a+"hi"));   // bind(&a,nMF(decM)).bind(&b,nMF(incLiftedM)).bind(&c, nMF(decM)).ret(&d, a+b+c+d);
        };
#endif
        qDebug() << "before run, all in one string ="+(a+b+c+d) << endl;

        result = WriterT::run(4, prog);
        cout << "Writer run(4, prog): " << qPrintable(result.show()) << "\n";
        //delete y;

        qDebug() << "after run, all in one string ="+(a+b+c+d) << endl;

#if 0 /// got this far?

    YES!
    //Mext_Func append = [&a,&b](){ return a()+b(); }; //as an exercise, this allows us to use only two "log" (capturing function) variables,
    //and "append" as we go. Also shown: how to create a varible function (a "comment from here)

    prog =           incM        .bind(
            &a,   nMF(decM)      ).bind(
            &b,   Writer<int,QString>::ret(a+b)  ).bind(
            &a,   nMF(incLiftedM)).bind(
            &b,   Writer<int,QString>::ret(a+b+"+M-- incLiftedM ] --M+")).bind( //insert comment in log, nothing to capture from incLiftedM
            &a,   nMF(decM)      ).bind(
            &b,   Writer<int,QString>::ret(a+b));

    result = WriterT::run(0, prog);   //incM.returnM("return text")));
    cout << "Writer run(see log, prog): " << qPrintable(result.show()) << "\n";
#endif // got this far!

    // ####################################################################################
    // ## invoke a monad run in a closure

    function<W_result(int x)> w;
    w = [prog](int x){ return prog.run(x, prog); };

    result = w(2);
    cout << "01 Writer run in lambda closure: " << qPrintable(result.show()) << "\n";


    // ####################################################################################
    // ## invoke a monad built in closure 02, run in a closure

    AMonad progM = ([]()
    {
        MextF txtF = [](){ return QString ("-- text from chain build closure space 02--"); };
        return AMonad::ret(txtF);
        //note, The Monad returned is created by executing the above function, Monad::ret now, in this build closure
    }
    )(); //build chain now

    w = [progM](int x){ return progM.run(x); };

    result = w(0);
    cout << "02 Monad run in lambda closure: " << qPrintable(result.show()) << "\n";


    function<W_result(int)> fM;
    // ####################################################################################
    // ## example LOGGER   -- from slide set ---

    AMonad incMM(add1);

    fM = [incMM](int x)
        {
            MextF mv_a, mv_b;

            mv_b = [&mv_a, &mv_b](){ return mv_a() + mv_b(); };

            return incMM.bind(&mv_a,incMM).bind(&mv_b, AMonad::ret(mv_b)).run(x);
        };

    result = fM(0);
    cout << "## example LOGGER ##: " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // ## test the monad laws
    /*****************************************************************

    http://www.haskell.org/haskellwiki/Monad_Laws

All instances of the Monad class should obey:

   1. "Left identity":
      return a >>= f  ≡  f a
   2. "Right identity":
      m >>= return  ≡  m
   3. "Associativity":
      (m >>= f) >>= g  ≡  m >>= (\x -> f x >>= g)

    Let us re-write the laws in do-notation:

    1. do { x' <- return x            do { f x
          ; f x'               ≡         }
          }
       or
       return x >>= \x' -> f x'

    2. do { x <- m             ≡      do { m
          ; return x }                   }

    3. do { y <- do { x <- m          do { x <- m
                    ; f x                ; do { x <- m
                    }          ≡              ; g y
          ; g y                               }
          }                              }

                                      do { x <- m
                                         ; y <- f x
                               ≡         ; g y
                                         }

    *****************************************************************/
    MextF law_ext = [](){ return "Monad law problem"; };

    // ####################################################################################
    // # 1.
    //FofM law1_f = [](int n){ return M_result<int,QString>(43,"left identity"); };
    FofM law1_f = [](int n){ return M_result<int,QString>(n+1,"left identity"); };
    AMonad law1_m (law1_f); // construct a monad with this function

    // left do{}'s function
    fM = [law_ext, law1_m](int testInit)
        {
            MextF x = law_ext; // x, reference monad variable
            MextF x_; // x' variable of monad producing function - also variable "capturing" value of (>>=)'s left monad's monad variable
            MF f = MFUNCT(law1_m);

            return AMonad::ret(x).bind(&x_, f).run(testInit);
        };

    result = fM(0); //run it
    cout << "## Monad law 1. left   ##: " << qPrintable(result.show()) << "\n";

    // right do{}'s function
    fM = [law_ext, law1_m](int testInit)
        {
            MextF x = [law_ext](){ return law_ext(); }; //reference monad variable
            MF f = MFUNCT(law1_m);

            return (f(x)).run(testInit);
        };

    result = fM(0); //run it
    cout << "## Monad law 1. right ##: " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // # 2.
    FofM law2_f = [](int n){ return M_result<int,QString>(43,"right identity"); };
    AMonad law2_m (law2_f);

    // left do{}'s function
    fM = [law2_m](int testInit)
        {
            AMonad m = law2_m;

            return m.bind(NULL, AMonad::ret).run(testInit);
        };

    result = fM(0); //run it
    cout << "## Monad law 2. left   ##: " << qPrintable(result.show()) << "\n";

    // right do{}'s function
    fM = [law2_m](int testInit)
        {
            AMonad m = law2_m;

            return m.run(testInit);
        };

    result = fM(0); //run it
    cout << "## Monad law 2. right ##: " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // # 3.
    FofM law3_f = [](int n){ return M_result<int,QString>(43,"associativity"); };
    AMonad law3_m (law3_f);
    MF fm = [](MextF &par){ return AMonad ([par](int n){ return M_result<int,QString>(n+1,"[ f("+QString::number(n)+","+ par()+") ]"); }); };
    MF gm = [](MextF &par){ return AMonad ([par](int n){ return M_result<int,QString>(n-1,"[ g("+QString::number(n)+","+ par()+") ]"); }); };

    // left do{}'s function
    // ( m >>= \x -> f x ) >>= \y -> g y
    fM = [law3_m, fm, gm](int testInit)
        {
            AMonad m = law3_m; //just for local name convenience
            MF f = fm;
            MF g = gm;
            MextF x,y; // variable of monad producing function (type MF) - also variable "capturing" value of (>>=)'s left monad's monad variable

            AMonad subM = m.bind(NULL, f);

            return subM.bind(NULL, g) .run(testInit);
        };

    result = fM(0); //run it
    cout << "## Monad law 3. left       ##: " << qPrintable(result.show()) << "\n";

    // right do{}'s function
    // m >>= \x -> f x >>= \y -> g y
    fM = [law3_m, fm, gm](int testInit)
        {
            AMonad m = law3_m;
            MF f = fm;
            MF g = gm;
            MextF x,y; // variable of monad producing function - also variable "capturing" value of (>>=)'s left monad's monad variable
            //x = law_ext; //shouldn't be needed, if early evaluations is avoided


            AMonad subM = AMonad::f2m(f, x); //  is: \x -> f x
                   subM = subM.bind(NULL, g);


            return m.bind(&x, subM).run (testInit);

            ///* wrong!, early evaluation of sub do: */ return m.bind(&x, f(x).bind(&y, g, y)).run(testInit);
            //return m.bind(&x, (Monad(Monad::unit)).bind(&z, f, x)).bind(&y, g, y).run(testInit);

        };

    result = fM(0); //run it
    cout << "## Monad law 3. right a. ##: " << qPrintable(result.show()) << "\n";

    // right do{}'s function
    // m >>= \x -> f x >>= \y -> g y
    fM = [law3_m, fm, gm](int testInit)
        {
            AMonad m = law3_m;
            MF f = fm;
            MF g = gm;
            MextF x,y; // variable of monad producing function - also variable "capturing" value of (>>=)'s left monad's monad variable
            //x = law_ext; //shouldn't be needed, if early evaluations is avoided

#if 1 // same as 0, but more apparent to the form: \n -> z n
            return m.bind(&x, f, x).bind(&y, g, y).run(testInit);
#else
            return m.bind(&x, f)   .bind(&y, g).run(testInit);
            //since x and y are not used in the chain, the above is the same as left form, without grouping (subM)
            //as:  m.bind(NULL, f) .bind(NULL, g).run(testInit);
#endif
        };

    result = fM(0); //run it
    cout << "## Monad law 3. right b. ##: " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // ## example chain 01

    AMonad decMM(dec1);

    fM = [incMM, decMM](int x)
        {
            MextF mv_a, mv_b, mv_c;

            mv_a = [](){ return "nuts"; };
            mv_b = mv_a;

            //myReturn is an MF constructed to apply other than the last monad variable to Monad::ret.
            //mv_c is applied, instead of ignored (which gets the value of mv_b in the chain example.
            mv_c = [&mv_a, &mv_b](){ return mv_a() + mv_b() + "--in chain txt--"; };
            //MF myReturn = [&mv_c](MextF &ignored){ return Monad::ret( mv_c ); };

            return incMM.bind(&mv_a,decMM).bind(&mv_b, AMonad::ret( mv_c )).run(x);
            //mv_a hooks the monad variable produced by incMM
            //mv_b hooks the monad variable produced by decMM
            //
        };

    result = fM(0);
    cout << "## example chain 01 ##: " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // # example chain 02
    function <int(int)> pureF = plainIncr1;
    fM = [pureF, fm, gm](int testInit)
        {

            MF f = fm;
            //recall, these functions produce monads having functions with monad variable output dependent on both underlying and
            // monad variables of the previous monad in the chain.
            MF g = gm;
            MextF v; // variable of monad producing function - also variable "capturing" value of (>>=)'s left monad's monad variable

            AMonad m (AMonad::lift(pureF));
            m = m.bind(&v,f);
            m = m.bind(&v,g);
            m = m.bind(&v,AMonad::ret);

            return m.run (testInit);
        };

    result = fM(0); //run it
    cout << "## example chain 02 ##: " << qPrintable(result.show()) << "\n";

    // ####################################################################################
    // # example chain 03
    fM = [fm, gm](int testInit)
        {

            MF f = fm;
            //recall, these functions produce monads having functions with monad variable output dependent on both underlying and
            // monad variables of the previous monad in the chain.
            MF g = gm;
            MextF v,x; // variable of monad producing function - also variable "capturing" value of (>>=)'s left monad's monad variable

            v = [](){ return QString("start"); };
            x = v;

            AMonad m = f(v);
            m = m.bind(&v,g);
#if 0 //not convenient?
            MF endOut = [&x](MextF &v_internal){ MextF t = [&v_internal, &x](){ return v_internal()+x(); }; return Monad::ret(t); };
            m = m.bind(&v, endOut);
#else
            MextF t = [&v, &x](){ return x()+v()+"end"; };
#if 0 //less convenient
            m = m.bind(&v, Monad::ret);
            m = m.bind(Monad::ret(t));
#else //most convenient
            m = m.bind(&v, AMonad::ret, t);
#endif
#endif
            return m.run (testInit);
        };

    result = fM(0); //run it
    cout << "## example chain 02 ##: " << qPrintable(result.show()) << "\n";
//------------
    FofM fff = bindx(bindx(add1,add1),add1);
    result = fff(1);
    cout << "## example FofM chain ##: " << qPrintable(result.show()) << "\n";
//------------
    return 0;
}
