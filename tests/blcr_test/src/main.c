#include <stdio.h>
#include <unistd.h>

int main(int argc, char *argv[])
{
  int count=0;
  pid_t my_pid;
  my_pid = getpid();
  printf("PID: %d\n", my_pid);
  while(++count <= 10000)
    {
      if( (count % 80) == 1)
	printf("\n%d",count);
      else
	printf(".");

      usleep(10000);
    }
  return 0;
}
