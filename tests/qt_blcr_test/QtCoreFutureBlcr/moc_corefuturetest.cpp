/****************************************************************************
** Meta object code from reading C++ file 'corefuturetest.h'
**
** Created: Wed Sep 15 15:18:10 2010
**      by: The Qt Meta Object Compiler version 62 (Qt 4.7.0)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "corefuturetest.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'corefuturetest.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 62
#error "This file was generated using the moc from 4.7.0. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
static const uint qt_meta_data_CoreFutureTest[] = {

 // content:
       5,       // revision
       0,       // classname
       0,    0, // classinfo
       4,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       2,       // signalCount

 // signals: signature, parameters, type, tag, flags
      16,   15,   15,   15, 0x05,
      24,   15,   15,   15, 0x05,

 // slots: signature, parameters, type, tag, flags
      35,   15,   15,   15, 0x0a,
      47,   15,   15,   15, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_CoreFutureTest[] = {
    "CoreFutureTest\0\0error()\0finished()\0"
    "startTest()\0handleFinished()\0"
};

const QMetaObject CoreFutureTest::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_CoreFutureTest,
      qt_meta_data_CoreFutureTest, 0 }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &CoreFutureTest::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *CoreFutureTest::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *CoreFutureTest::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CoreFutureTest))
        return static_cast<void*>(const_cast< CoreFutureTest*>(this));
    return QObject::qt_metacast(_clname);
}

int CoreFutureTest::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: error(); break;
        case 1: finished(); break;
        case 2: startTest(); break;
        case 3: handleFinished(); break;
        default: ;
        }
        _id -= 4;
    }
    return _id;
}

// SIGNAL 0
void CoreFutureTest::error()
{
    QMetaObject::activate(this, &staticMetaObject, 0, 0);
}

// SIGNAL 1
void CoreFutureTest::finished()
{
    QMetaObject::activate(this, &staticMetaObject, 1, 0);
}
QT_END_MOC_NAMESPACE
