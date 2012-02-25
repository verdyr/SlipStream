/*
 * shm-client - client program to demonstrate shared memory.
 */

#include <grp.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <stdio.h>

#include <libcrtest.h>

#define SHMSZ     27

#define DIRNAME "./sandbox"
#define SHMCREATED DIRNAME "/shm-created"
#define FNAME DIRNAME "/shm-ok"

void docreat(char *fnam, int mode)
{
	int ret = creat(fnam, mode);
	if (ret == -1) {
		perror("create"); /* though noone can hear us */
		exit(2);
	}
}

main()
{
    int shmid;
    key_t key;
    char *shm, *s;
    int i = 0;
    pid_t pid;
    
    if (!move_to_cgroup("freezer", "1", getpid())) {
      printf("Failed to move myself to cgroup /1\n");
      exit(1);
    }

    mkdir(DIRNAME, 0755);
    
    docreat(SHMCREATED,  S_IRUSR | S_IWUSR);
    
    /*
     * We need to get the segment named
     * "5678", created by the server.
     */
    key = 5678;
    
    /*
     * Locate the segment.
     */
    if ((shmid = shmget(key, SHMSZ, 0666)) < 0) {
        perror("shmget");
        exit(1);
    }

    /*
     * Now we attach the segment to our data space.
     */
    if ((shm = shmat(shmid, NULL, 0)) == (char *) -1) {
        perror("shmat");
        exit(1);
    }

    /*
     * Now read what the server put in the memory.
     */

    fclose(stderr);
    fclose(stdin);
    fclose(stdout);

    while (i++ < 2)
      {
	char newchar;
	for (s = shm; *s != NULL; s++)
	  //putchar(*s);
	
	usleep(100000);
      }

    /*
     * Finally, change the first character of the 
     * segment to '*', indicating we have read 
     * the segment.
     */
    *shm = '*';

    exit(0);
}
