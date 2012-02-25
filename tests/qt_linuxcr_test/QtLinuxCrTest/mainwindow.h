#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QTimer>
namespace Ui {
    class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

protected:
    void changeEvent(QEvent *e);
protected slots:
    void timeout();
private:
    Ui::MainWindow *ui;
    QTimer *m_timer;
    int m_dialValue;
private slots:
    void on_pushButton_clicked();
};

#endif // MAINWINDOW_H
