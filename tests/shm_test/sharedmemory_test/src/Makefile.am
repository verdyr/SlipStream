bin_PROGRAMS = shm_server shm_client

# Compiler flags
shm_server_CFLAGS = -Wall
shm_server_CFLAGS += -I$(top_srcdir)/include -I  -I/usr/include

shm_client_CFLAGS = -Wall
shm_client_CFLAGS += -I$(top_srcdir)/include -I  -I/usr/include -lcr_run


ACLOCAL_AMFLAGS = -I -I$(top_srcdir)/m4

# Sources to compile. These should be in alphabetical order.
shm_server_SOURCES = shm_server.c

shm_client_SOURCES = shm_client.c

