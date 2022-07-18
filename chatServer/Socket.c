#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/time.h>
#include <string.h>
#include <time.h>
#include "MT.h"

int flag = 0;

int getflag() {
	int f;
	f = flag;
	flag = 0;
	return f;
}

// "MT.h"
void initRand() {
	init_genrand((unsigned)time(NULL));
}

double random() {
	return genrand_real2();
}

// <string.h>
int strLength(char* str) {
	return strlen(str);
}

// <unistd.h>
void Sleep_(int us) {
	usleep(us);
}

// <sys/ioctl.h>
void nonBlocking(int sock) {
	int val = 1;
	ioctl(sock, FIONBIO, &val);
}

// <errno.h>
int notRecv(void) {
	return (errno == EAGAIN ? 1 : 0);
}

int errorNum(void) {
	return errno;
}

// <sys/types.h> and <sys/socket.h>
void reuse(int sock) {
	int yes = 1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (const char *)&yes, sizeof(yes));
}

int connectS(int sock, char* ip, int port) {
	struct sockaddr_in addr;
	
	addr.sin_family = PF_INET;
	addr.sin_addr.s_addr = inet_addr(ip);
	addr.sin_port = htons(port);
	return connect(sock, (struct sockaddr *)&addr, sizeof(addr));
}

int TCPSocket(void) {
	return socket(PF_INET, SOCK_STREAM, 0);
}

int UDPSocket(void) {
	return socket(PF_INET, SOCK_DGRAM, 0);
}

int bindNIP(int sock, int port) {
	struct sockaddr_in addr;
	addr.sin_family = PF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.s_addr = INADDR_ANY;
	return bind(sock, (struct sockaddr *)&addr, sizeof(addr));
}

int bindIP(int sock, int port, char* ip) {
	struct sockaddr_in addr;
	addr.sin_family = PF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.s_addr = inet_addr(ip);
	return bind(sock, (struct sockaddr *)&addr, sizeof(addr));
}

int listenNB(int sock) {
	return listen(sock, 10);
}

int acceptNI(int sock) {
	struct sockaddr_in client;
	socklen_t len = sizeof(client);
	return accept(sock, (struct sockaddr *)&client, &len);
}

int acceptInfo(int sock,int* port, char* ip) {
	int s;
	struct sockaddr_in client;
	socklen_t len = sizeof(client);
	s = accept(sock, (struct sockaddr *)&client, &len);
	if (port != NULL) {
		*port = ntohs(client.sin_port);
	}
	if (ip != NULL) {
		strcpy(ip,inet_ntoa(client.sin_addr));
	}
	return s;
} 

void closeSocket(int sock) {
	close(sock);
}

int selectNT(int sock, fd_set* fds) {
	struct timeval tv;
	
	tv.tv_sec = 0;
	tv.tv_usec = 0;
	
	return select(sock+1, fds, NULL, NULL, &tv);
}

int selectT(int sock, int s,int us, fd_set* fds) {
	struct timeval tv;
	
	tv.tv_sec = s;
	tv.tv_usec = us;
	
	return select(sock+1, fds, NULL, NULL, &tv);
}

long recvfromS(int sock, char* buf, int size, char* senderstr, int* port) {
	struct sockaddr_in senderinfo;
	socklen_t addrlen;
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

long recvS(int sock, char* buf, int size) {
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

long sendS(int sock, char* buf, int size) {
	return send(sock, buf, size, 0);
}

long recvI(int sock, int* buf, int size) {
	int i;
	for(i = 0; i < size; i++) {
		buf[i] = 0;
	}
	return recv(sock, buf, size, 0);
}

long sendI(int sock, int* buf, int size) {
	return send(sock, buf, size, 0);
}

long recvfromI(int sock, int* buf, int size, char* senderstr, int* port) {
	struct sockaddr_in senderinfo;
	socklen_t addrlen;
	long rtn;
	int i;
	for(i = 0; i < size; i++) {
		buf[i] = 0;
	}
	
	addrlen = sizeof(senderinfo);
	rtn = recvfrom(sock, buf, size, 0, (struct sockaddr *)&senderinfo, &addrlen);
	if (senderstr != NULL) {
		inet_ntop(AF_INET, &senderinfo.sin_addr, senderstr, sizeof(senderstr));
	}
	if (port != NULL) {
		*port = ntohs(senderinfo.sin_port);
	}
	return rtn;
}

long recvD(int sock, double* buf, int size) {
	int i;
	for(i = 0; i < size; i++) {
		buf[i] = 0;
	}
	return recv(sock, buf, size, 0);
}

long sendD(int sock, double* buf, int size) {
	return send(sock, buf, size, 0);
}

long recvfromD(int sock, double* buf, int size, char* senderstr, int* port) {
	struct sockaddr_in senderinfo;
	socklen_t addrlen;
	long rtn;
	int i;
	for(i = 0; i < size; i++) {
		buf[i] = 0;
	}
	
	addrlen = sizeof(senderinfo);
	rtn = recvfrom(sock, buf, size, 0, (struct sockaddr *)&senderinfo, &addrlen);
	if (senderstr != NULL) {
		inet_ntop(AF_INET, &senderinfo.sin_addr, senderstr, sizeof(senderstr));
	}
	if (port != NULL) {
		*port = ntohs(senderinfo.sin_port);
	}
	return rtn;
}

void FD_ZERO_(fd_set* fds) {
	FD_ZERO(fds);
}

void FD_SET_(int sock, fd_set* fds) {
	FD_SET(sock, fds);
}

int FD_ISSET_(int sock, fd_set* fds) {
	return FD_ISSET(sock, fds);
}

// <arpa/inet.h>
unsigned long inetAddr(char* cp) {
	return inet_addr(cp);
}

unsigned long htonLong(unsigned long hostlong) {
	return htonl(hostlong);
}

unsigned short htonShort(unsigned short hostshort) {
	return htons(hostshort);
}

unsigned long ntohLong(unsigned long netlong) {
	return ntohl(netlong);
}

unsigned short ntohShort(unsigned short netshort) {
	return ntohs(netshort);
}

// <sys/time.h>
int CanIRecv(int fd) {
	fd_set fdset;
	struct timeval timeout;
	FD_ZERO(&fdset);
	FD_SET(fd, &fdset);
	timeout.tv_sec = 0;
	timeout.tv_usec = 0;
	return(select(fd + 1, &fdset, NULL, NULL, &timeout));
}

int ReadNB(int fd, char *buff, int buffSize) {
	int i = CanIRecv(fd);
	if (i) {
		read(fd, buff, buffSize);
		buff[strlen(buff) - 1] = '\0';
	}
	return(i);
}

int getNB(char *buff, int buffsize) {
	return ReadNB(0, buff, buffsize);
}