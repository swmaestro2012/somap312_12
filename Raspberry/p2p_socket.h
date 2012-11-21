#ifndef _P2P_SOCKET_H_
#define _P2P_SOCKET_H_

#include <sys/socket.h>
#include <errno.h>
#include <string.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <stdio.h>

#define BUFSIZE 1024
#define CMDBUFSIZE 256

#define BUF_SEND_COMPLETED "send_completed"
#define BUF_SEND_CONNETED "Hello\n"
#define BUF_SEND_ACK "OK\n"

#define GET_FILE_DIR "GET_FILE_DIR"

#define FILE_PORT 8988
#define REMOTE_PORT 9897

#define GO_ADDRESS "192.168.49.1"

typedef int					SOCKET;
typedef struct sockaddr		SOCKADDR;
typedef struct sockaddr_in 	SOCKADDR_IN;

int p2p_socket(void);
int recv_file(int fdSocket, char* fileName);
int recv_remote(int remoteSocket);
void showPPT(char *);
//int open_server(int *fdSocket, int flag);

#endif
