#include <stdio.h>
#include <sys/types.h>
#include <winsock2.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include "MT.h"
#define LEN 1024

#ifdef __cplusplus
extern "C"{
#endif

int flag = 0;

__declspec(dllexport)
int getflag() {
	int f;
	f = flag;
	flag = 0;
	return f;
}

// "MT.h"
__declspec(dllexport)
void initRand() {
	init_genrand((unsigned)time(NULL));
}

__declspec(dllexport)
double random() {
	return genrand_real2();
}

// <string.h>
__declspec(dllexport)
int strLength(char* str) {
	return strlen(str);
}

// <unistd.h>
__declspec(dllexport)
void Sleep_(int us) {
	Sleep(us/1000);
}

// <sys/ioctl.h>
__declspec(dllexport)
void nonBlocking(int sock) {
	u_long val=1;
	ioctlsocket(sock, FIONBIO, &val);
}

// <errno.h>
__declspec(dllexport)
int notRecv(void) {
	return (WSAGetLastError() == WSAEWOULDBLOCK ? 1 : 0);
}

__declspec(dllexport)
int errorNum(void) {
	return errno;
}

// <sys/types.h> and <sys/socket.h>
__declspec(dllexport)
void reuse(SOCKET sock) {
	BOOL yes = 1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (const char *)&yes, sizeof(yes));
}

__declspec(dllexport)
void closeSocket(SOCKET sock) {
	closesocket(sock);
	WSACleanup();
}

__declspec(dllexport)
SOCKET TCPSocket(void) {
	WSADATA wsaData;
	WSAStartup(MAKEWORD(2,0), &wsaData);
	return socket(PF_INET, SOCK_STREAM, 0);
}

__declspec(dllexport)
SOCKET UDPSocket(void) {
	WSADATA wsaData;
	WSAStartup(MAKEWORD(2,0), &wsaData);
	return socket(PF_INET, SOCK_DGRAM, 0);
}

__declspec(dllexport)
int bindNIP(SOCKET sock, int port) {
	struct sockaddr_in addr;
	addr.sin_family = PF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.S_un.S_addr = INADDR_ANY;
	return bind(sock, (struct sockaddr *)&addr, sizeof(addr));
}

__declspec(dllexport)
int bindIP(SOCKET sock, int port, char* ip) {
	struct sockaddr_in addr;
	addr.sin_family = PF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.S_un.S_addr = inet_addr(ip);
	return bind(sock, (struct sockaddr *)&addr, sizeof(addr));
}

__declspec(dllexport)
int listenNB(SOCKET sock) {
	return listen(sock, 10);
}

__declspec(dllexport)
int connectS(SOCKET sock, char* ip, int port) {
	struct sockaddr_in addr;
	
	addr.sin_family = PF_INET;
	addr.sin_addr.S_un.S_addr = inet_addr(ip);
	addr.sin_port = htons(port);
	return connect(sock, (struct sockaddr *)&addr, sizeof(addr));
}

__declspec(dllexport)
int acceptNI(SOCKET sock) {
	struct sockaddr_in client;
	int len = sizeof(client);
	return accept(sock, (struct sockaddr *)&client, &len);
}

__declspec(dllexport)
int acceptInfo(SOCKET sock,int* port, char* ip) {
	int s;
	struct sockaddr_in client;
	int len = sizeof(client);
	s = accept(sock, (struct sockaddr *)&client, &len);
	if (port != NULL) {
		*port = ntohs(client.sin_port);
	}
	if (ip != NULL) {
		strcpy(ip, inet_ntoa(client.sin_addr));
	}
	return s;
} 

__declspec(dllexport)
int selectNT(SOCKET sock, fd_set* fds) {
	struct timeval tv;
	
	tv.tv_sec = 0;
	tv.tv_usec = 0;
	
	return select(sock+1, fds, NULL, NULL, &tv);
}

__declspec(dllexport)
int selectT(SOCKET sock, int s,int us, fd_set* fds) {
	struct timeval tv;
	
	tv.tv_sec = s;
	tv.tv_usec = us;
	
	return select(sock+1, fds, NULL, NULL, &tv);
}

__declspec(dllexport)
long recvfromS(SOCKET sock, char* buf, int size, char* senderstr, int* port) {
	struct sockaddr_in senderinfo;
	int addrlen;
	long rtn;
	int i;
	for(i = 0; i < size; i++) {
		buf[i] = '\0';
	}
	
	addrlen = sizeof(senderinfo);
	rtn = recvfrom(sock, buf, size, 0, (struct sockaddr *)&senderinfo, &addrlen);
	if (strcmp(buf, "") == 0) {
		rtn = -1L;
		flag = 1;
	}
	if (senderstr != NULL) {
		strcpy(senderstr, inet_ntoa(senderinfo.sin_addr));
	}
	if (port != NULL) {
		*port = ntohs(senderinfo.sin_port);
	}
	return rtn;
}

__declspec(dllexport)
long recvS(SOCKET sock, char* buf, int size) {
	int i;
	long rtn;
	for(i = 0; i < size; i++) {
		buf[i] = '\0';
	}
	rtn = recv(sock, buf, size, 0);
	if (strcmp(buf, "") == 0) {
		rtn = -1L;
		flag = 1;
	}
	return rtn;
}

__declspec(dllexport)
long sendS(SOCKET sock, char* buf, int size) {
	return send(sock, buf, size, 0);
}

__declspec(dllexport)
long recvI(SOCKET sock, int* buf, int size) {
	int i;
	char buffer[LEN];
	long rtn;
	*buf = 0;
	rtn = recv(sock, buffer, size, 0);
	*buf = atoi(buffer);
	return rtn;
}

__declspec(dllexport)
long sendI(SOCKET sock, int* buf, int size) {
	char buffer[LEN];
	snprintf(buffer, LEN, "%d", *buf);
	return send(sock, buffer, size, 0);
}

__declspec(dllexport)
long recvfromI(SOCKET sock, int* buf, int size, char* senderstr, int* port) {
	struct sockaddr_in senderinfo;
	int addrlen;
	long rtn;
	char buffer[LEN];
	int i;
	*buf=0;
	
	addrlen = sizeof(senderinfo);
	rtn = recvfrom(sock, buffer, size, 0, (struct sockaddr *)&senderinfo, &addrlen);
	if (senderstr != NULL) {
		strcpy(senderstr, inet_ntoa(senderinfo.sin_addr));
	}
	if (port != NULL) {
		*port = ntohs(senderinfo.sin_port);
	}
	*buf = atoi(buffer);
	return rtn;
}

__declspec(dllexport)
long recvD(SOCKET sock, double* buf, int size) {
	int i;
	char buffer[LEN];
	long rtn;
	*buf = 0.0;
	rtn = recv(sock, buffer, size, 0);
	*buf = atof(buffer);
	return rtn;
}

__declspec(dllexport)
long sendD(SOCKET sock, double* buf, int size) {
	char buffer[LEN];
	snprintf(buffer, LEN, "%f", *buf);
	return send(sock, buffer, size, 0);
}

__declspec(dllexport)
long recvfromD(SOCKET sock, double* buf, int size, char* senderstr, int* port) {
	struct sockaddr_in senderinfo;
	int addrlen;
	long rtn;
	char buffer[LEN];
	int i;
	*buf=0.0;
	
	addrlen = sizeof(senderinfo);
	rtn = recvfrom(sock, buffer, size, 0, (struct sockaddr *)&senderinfo, &addrlen);
	if (senderstr != NULL) {
		strcpy(senderstr, inet_ntoa(senderinfo.sin_addr));
	}
	if (port != NULL) {
		*port = ntohs(senderinfo.sin_port);
	}
	*buf = atof(buffer);
	return rtn;
}

__declspec(dllexport)
void FD_ZERO_(fd_set* fds) {
	FD_ZERO(fds);
}

__declspec(dllexport)
void FD_SET_(int sock, fd_set* fds) {
	FD_SET(sock, fds);
}

__declspec(dllexport)
int FD_ISSET_(int sock, fd_set* fds) {
	return FD_ISSET(sock, fds);
}

// <arpa/inet.h>
__declspec(dllexport)
unsigned long inetAddr(char* cp) {
	return inet_addr(cp);
}

__declspec(dllexport)
u_long htonLong(u_long hostlong) {
	return htonl(hostlong);
}

__declspec(dllexport)
u_short htonShort(u_short hostshort) {
	return htons(hostshort);
}

__declspec(dllexport)
u_long ntohLong(u_long netlong) {
	return ntohl(netlong);
}

__declspec(dllexport)
u_short ntohShort(u_short netshort) {
	return ntohs(netshort);
}

// <sys/time.h>
__declspec(dllexport)
int CanIRecv(int fd) {
	fd_set fdset;
	struct timeval timeout;
	FD_ZERO(&fdset);
	FD_SET(fd, &fdset);
	timeout.tv_sec = 0;
	timeout.tv_usec = 0;
	return(select(fd + 1, &fdset, NULL, NULL, &timeout));
}

__declspec(dllexport)
int ReadNB(int fd, char *buff, int buffSize) {
	int i = CanIRecv(fd);
	if (i) {
		recv(fd, buff, buffSize, 0);
		buff[strlen(buff) - 1] = '\0';
	}
	return(i);
}

__declspec(dllexport)
int getNB(char *buff, int buffsize) {
	return ReadNB(0, buff, buffsize);
}

#ifdef __cplusplus
}
#endif