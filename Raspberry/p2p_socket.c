#include "p2p_test.h"
#include "p2p_socket.h"
#include "keymap.h"

#define DBGPRINT printf

Display *display;
Window winRoot;
Window windowID = 0;

int p2p_socket(void)
{
	int ret = 0;
	int socketID = 0;
	struct sockaddr_in server_addr;
	char fileName[CMDBUFSIZE]={0};

	printf("P2P_SOCKET Function...\n");

	socketID = socket( AF_INET, SOCK_STREAM, 0 );
	if(socketID == -1)
	{
		perror("Create Soket failed...\n");
		goto OUT;
	}
	printf("Create Socket... \n");
	
	memset( &server_addr, 0, sizeof(server_addr) );
	server_addr.sin_family		= AF_INET;
	server_addr.sin_port 		= htons(FILE_PORT);
	server_addr.sin_addr.s_addr	= inet_addr(GO_ADDRESS);

	printf("Conneting server...\n");
	while(connect(socketID, (struct sockaddr*)&server_addr, sizeof(server_addr))==-1)
	{
		printf("Not found server...\n");
		usleep(0.2 * SEC);
		
		memset( &server_addr, 0, sizeof(server_addr) );
		server_addr.sin_family		= AF_INET;
		server_addr.sin_port 		= htons(FILE_PORT);
		server_addr.sin_addr.s_addr	= inet_addr(GO_ADDRESS);
	}

	printf("Conneted!!!\n");

	printf("Reciving file... \n");
	ret = recv_file(socketID, fileName);
	if(ret != 0)
	{
		printf("File transfer completed. \n");
	}
	else
	{
		perror("File transfer failed. \n");
		goto OUT;
	}
	if(socketID != -1)
	{
		close(socketID);
	}
	
	printf("File socket closed...\n");
	socketID = socket( AF_INET, SOCK_STREAM, 0 );
	if(socketID == -1)
	{
		perror("Create Soket failed...\n");
		goto OUT;
	}
	printf("Create Socket... \n");
		
	memset( &server_addr, 0, sizeof(server_addr) );
	server_addr.sin_family		= AF_INET;
	server_addr.sin_port 		= htons(REMOTE_PORT);
	server_addr.sin_addr.s_addr	= inet_addr(GO_ADDRESS);

	printf("Remote socket Start...\n");

	printf("Conneting server...\n");
	while(connect(socketID, (struct sockaddr*)&server_addr, sizeof(server_addr))==-1)
	{
		printf("Not found remote server...\n");
		usleep(0.2 * SEC);
		
		memset( &server_addr, 0, sizeof(server_addr) );
		server_addr.sin_family		= AF_INET;
		server_addr.sin_port 		= htons(REMOTE_PORT);
		server_addr.sin_addr.s_addr	= inet_addr(GO_ADDRESS);
	}

	printf("Conneted!!!\n");

	if(pptProcessing(fileName)==0)
	{
		perror("PPT Processing Error...\n");
		goto OUT;
	}

	printf("Reciving remote control signal... \n");
	ret = recv_remote(socketID);
	if(ret != 0)
	{
		printf("PPT Quit. \n");
	}
	else
	{
		perror("Exception quit... \n");
		goto OUT;
	}

	printf("Remote socket closed...\n");

OUT:
	if(socketID != -1)
	{
		close(socketID);
	}
	return 0;
}
void showPPT(char *fileName)
{
	char cmd[CMDBUFSIZE];

	memset(cmd, 0, sizeof(cmd));
	sprintf(cmd, "calligrastage ./%s/%s &",GET_FILE_DIR,fileName);
	system(cmd);
}
int pptProcessing(char *fileName)
{
	showPPT(fileName);
	if(MakeDisplay(&display, &winRoot)==0)
	{
		perror("Can not Make Display...\n");
		return 0;
	}
	while(windowID == 0)
	{

		windowID = FindWindow(fileName);
		usleep(0.1*SEC);
	}
	KeyProcessing(display, winRoot, windowID, KEY_F5);
	XCloseDisplay(display);
	return 1;
}

int keyProcessing(int keyCode)
{
	if(MakeDisplay(&display, &winRoot)==0)
	{
		perror("Can not Make Display...\n");
		return 0;
	}
	KeyProcessing(display, winRoot, windowID, keyCode);
	XCloseDisplay(display);

	return 1;
}
#if 0
int p2p_socket_server(void)
{
	int ret = 0;
	int fdSocket = 0;
	struct sockaddr_in client_addr;

	printf("Opening server... \n");
	ret = open_server(&fdSocket, 1);
	printf("%d \n", ret);
	if(ret != 0)
	{
		goto OUT;
	}
	
	printf("Accepting... \n");
	while(1)
	{
		struct sockaddr serveraddr;
		int fdClient;
		int lenAddress;

		fdClient = accept(fdSocket, (struct sockaddr *)&serveraddr, &lenAddress);
		if(fdClient != -1)
		{
			printf("Accepted. \n");
		}
		else
		{
			perror("Accept failed. \n");
			continue;
		}

		printf("Reciving file... \n");
		ret = recv_file(fdClient);
		if(ret != 0)
		{
			printf("File transfer completed. \n");
		}
		else
		{
			perror("File transfer failed. \n");
			continue;
		}
	}

OUT:
	if(fdSocket != 0)
	{
		close(fdSocket);
	}

	return 0;
}

int open_server(int *fdSocket, int flag)
{
	int ret;
	struct sockaddr_in serveraddr;

	if(flag == 1)
	{
		printf("Make socket... \n");
	}
	*fdSocket = socket(AF_INET, SOCK_STREAM, 0);
	if(*fdSocket == -1)
	{
		return 1;
	}

	if(flag == 1)
	{
		printf("Setting socket... \n");
	}
	serveraddr.sin_family = AF_INET;
	serveraddr.sin_port = htons(PORT);
	serveraddr.sin_addr.s_addr = INADDR_ANY;
	bzero(&(serveraddr.sin_zero), 8);

	if(flag == 1)
	{
		printf("Binding... \n");
	}
	ret = bind(*fdSocket, (struct sockaddr *)&serveraddr, sizeof(struct sockaddr));
	if(ret == -1)
	{
		return 2;
	}

	if(flag == 1)
	{
		printf("Listening... \n");
	}
	ret = listen(*fdSocket, 10);
	if(ret == -1)
	{
		return 4;
	}

	return 0;
}
#endif
int recv_file(int fdSocket, char* fileName)
{
	int numRecvByte;
	int i;
	char buf[BUFSIZE];
	char cmd[CMDBUFSIZE];
	FILE* fout = NULL;

	printf("Starting File Socket...\n");
	if(send(fdSocket, BUF_SEND_CONNETED, strlen(BUF_SEND_CONNETED), 0)==-1)
	{
		perror("Conneted message Send fail...\n");
	}
	printf("%s send OK...\n",BUF_SEND_CONNETED);
	
	if((numRecvByte=recv(fdSocket, buf, BUFSIZE, 0))==-1)
	{
		perror("File name can not recive...\n");
	}
	strncpy(fileName, buf, numRecvByte-1);
	printf("File Name : %s",fileName);

	if(send(fdSocket, BUF_SEND_ACK, strlen(BUF_SEND_ACK), 0)==-1)
	{
		perror("ACK message Send fail...\n");
	}
	printf("%s send OK...\n",BUF_SEND_ACK);

	memset(cmd, 0, sizeof(cmd));
	sprintf(cmd,"mkdir %s",GET_FILE_DIR);
	system(cmd);

	memset(cmd, 0, sizeof(cmd));
	sprintf(cmd, "%s/%s",GET_FILE_DIR,fileName);

	printf("[%s]\n",cmd);
	fout = fopen(cmd, "wb");
	while(1)
	{
		// Receive data
		numRecvByte = recv(fdSocket, buf, BUFSIZE, 0);
		if(numRecvByte == -1)
		{
			DBGPRINT("Data receiving failed. \n");
			goto OUT;
		}
		else if(numRecvByte == 0)
		{
			// Receiving data completed.
			printf("Receiving data completed. \n");
			break;
		}

		// Write file
		fwrite(buf, numRecvByte, 1, fout);
	}

OUT:
	if(fout != NULL)
	{
		fclose(fout);
	}

	return (numRecvByte == 0);
}

int recv_remote(int remoteSocket)
{
	int numRecvByte;
	int i;
#define TEMPBUFSIZE 3
	char buf[TEMPBUFSIZE];
	
	printf("Starting Remote Socket...\n");
	if(send(remoteSocket, BUF_SEND_CONNETED, strlen(BUF_SEND_CONNETED), 0)==-1)
	{
		perror("Conneted message Send fail...\n");
	}
	printf("%s send OK...\n",BUF_SEND_CONNETED);

	printf("F5 key Sending...\n");
	while(1)
	{
		// Receive data
		printf("abc");
		numRecvByte = recv(remoteSocket, buf, TEMPBUFSIZE, 0);
		printf("def");
		printf("[%s]\n",buf);
		if(numRecvByte == -1)
		{
			DBGPRINT("Data receiving failed. \n");
			return 0;
		}
		else if(strncmp(buf,"L",1)==0)
		{
			keyProcessing(KEY_LEFT);
			printf("L\n");
			if(send(remoteSocket, BUF_SEND_ACK, strlen(BUF_SEND_ACK), 0)==-1)
			{
				perror("ACK message Send fail...\n");
			}
			printf("%s send OK...\n",BUF_SEND_ACK);
		}
		else if(strncmp(buf,"R",1)==0)
		{

			keyProcessing(KEY_RIGHT);
			printf("R\n");
			if(send(remoteSocket, BUF_SEND_ACK, strlen(BUF_SEND_ACK), 0)==-1)
			{
				perror("ACK message Send fail...\n");
			}
			printf("%s send OK...\n",BUF_SEND_ACK);
		}
		else if(strncmp(buf,"Q",1)==0)
		{
			system("killall calligrastage");
			printf("Q\n");
			if(send(remoteSocket, BUF_SEND_ACK, strlen(BUF_SEND_ACK), 0)==-1)
			{
				perror("ACK message Send fail...\n");
			}
			printf("%s send OK...\n",BUF_SEND_ACK);
			return 1;
		}
	}
}
