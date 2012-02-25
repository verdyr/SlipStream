#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QFile>
#include <QDebug>
#define DIRNAME "./sandbox"
#define SHMCREATED DIRNAME "/shm-created"
#define FNAME DIRNAME "/shm-ok"
extern "C" {
#include <libcrtest.h>
}

#if 0
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <wait.h>
#include <errno.h>
#include <string.h>
#include <malloc.h>
#include <dirent.h>
#include <sys/eventfd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include "utils.h"


#define MAXPATH 200
#define MAXLINE 400

static char *freezer_mnt;

char *freezer_mountpoint(void)
{
        if (freezer_mnt)
                return freezer_mnt;
        FILE *fmounts = fopen("/proc/mounts", "r");
        char line[MAXLINE];
        if (!fmounts)
                return NULL;
        while (fgets(line, MAXLINE, fmounts)) {
                char *options, *fstype, *mountpoint;
                options = fieldnumber(line, 3);
                fstype = fieldnumber(line, 2);
                mountpoint = fieldnumber(line, 1);
                if (!fstype || !options || !mountpoint) {
                        printf("missing fields in /proc/mounts entry\n");
                        do_exit(1);
                }
                if (strcmp(fstype, "cgroup"))
                        continue;
                if (!mount_entry_has_option(options, "freezer"))
                        continue;
                if (mount_entry_has_option(options, "ns")) {
                        printf("freezer is composed with ns subsystem.\n");
                        do_exit(1);
                }
                /* success */
                freezer_mnt = malloc(strlen(mountpoint)+1);
                strncpy(freezer_mnt, mountpoint, strlen(mountpoint)+1);
                break;
        }

        fclose(fmounts);
        return freezer_mnt;
}

static void create_cgroup(char *grp)
{
        char dirnam[MAXPATH];
        snprintf(dirnam, MAXPATH, "%s/%s", freezer_mountpoint(), grp);
        mkdir(dirnam, 0755);
}

/*
 * move process pid to subsys cgroup grp
 * return 0 on failure, 1 on success
 */
int move_to_cgroup(char *subsys, char *grp, int pid)
{
        char fname[MAXPATH];
        int rc;

        rc = access(CKPT_DRY_RUN, F_OK);
        if (rc == 0)
                return 1;
        if (strcmp(subsys, "freezer"))
                return 0;
        if (!freezer_mountpoint()) {
                printf("freezer cgroup is not mounted.\n");
                do_exit(1);
        }
        create_cgroup(grp);

        snprintf(fname, MAXPATH, "%s/%s/tasks", freezer_mountpoint(), grp);
        FILE *fout = fopen(fname, "w");
        if (!fout) {
                printf("Failed to open freezer taskfile %s\n", fname);
                return 0;
        }
        if (fprintf(fout, "%d\n", pid) <  0) {
                printf("Failed to write pid to taskfile\n");
                fclose(fout);
                return 0;
        }
        fflush(fout);
        fclose(fout);
        return 1;
}
#endif

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow),
    m_dialValue(0)
{
    ui->setupUi(this);
    m_dialValue = ui->dial->minimum();
    quint64 pid = QCoreApplication::applicationPid();
    ui->pidText->setText( QString("%1").arg(pid)  );

    if (!move_to_cgroup("freezer", "1", getpid())) {
            printf("Failed to move myself to cgroup /1\n");
            exit(1);
    }


    QFile file(QString(SHMCREATED));

    if( file.open(QIODevice::WriteOnly | QIODevice::Text) )
    {
//        if(!file.setPermissions( QFile::WriteUser | QFile::ReadUser | QFile::ExeUser | QFile::ReadGroup | QFile::ExeGroup  | QFile::ReadOther | QFile::ExeOther))
//        {
//            qDebug() << "Could not set permissions";
//        }
        QTextStream out(&file);
        out << pid;
        file.close();
    }
    else
    {
        qDebug() << "Could not create file: " << QString( SHMCREATED);
    }

    m_timer = new QTimer(this);
    connect(m_timer, SIGNAL(timeout()), this, SLOT(timeout()));
    m_timer->start(100);
    fclose(stdin);
    fclose(stdout);
    fclose(stderr);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::changeEvent(QEvent *e)
{
    QMainWindow::changeEvent(e);
    switch (e->type()) {
    case QEvent::LanguageChange:
        ui->retranslateUi(this);
        break;
    default:
        break;
    }
}

void MainWindow::on_pushButton_clicked()
{
    int number = (int) ui->lcdNumber->value();
    number++;
    ui->lcdNumber->display(number);
}
void MainWindow::timeout()
{
    m_dialValue++;
    if(m_dialValue > ui->dial->maximum())
        QCoreApplication::quit();

    ui->dial->setValue(m_dialValue);
    show();
}
