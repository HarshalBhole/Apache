# Apache
build a protocol mediator called Apache. This develops the DNS function and does the actual protocol mediation.

Apache will listen on a user-specified socket for HTTP requests and return a properly-formatted response with the preferred IP indicated. You must use InetAddress.GetAllByName(String) and use some logic to select a preferred IP from the list. You should explain your selection logic in your code comments. Your Apache must look and work exactly like this:

Server View

afsaccess1-> java Apache 3344      ## listens on afsaccess1:3344
Apache Listening on socket 3344
(1) Incoming client connection from [A.B.C.D:nnnnn] to me [E.F.G.H:nnnnn]
    REQ: www.berkeley.edu / RESP: 100.101.102.103
(2) Incoming client connection from [A.B.C.D:nnnnn] to me [E.F.G.H:nnnnn]
    REQ: www.hogwarts.edu / RESP: ERROR

Client i.e. browser View: suppose the user types www.berkeley.edu (Links to an external site.) into a browser, your Apache should return a response that renders in the browser like this. Note that the first few lines are the complete HTTP request and the last two lines will be added by your Apache.

GET /index.html HTTP/1.1
Host: www.berkeley.edu
User-Agent: Mozilla/5.0
Accept: text/xml,application/xml,application/xhtml+xml,text/html*/*
Accept-Language: en-us Accept-Charset: ISO-8859-1,utf-8
Connection: keep-alive 

DNS LOOKUP: www.berkeley.edu
Preferred IP: 22.33.44.55
you must use the parse() method to parse the input and set HOST, and you must use the dns() method which will call GetAllByName(). You may get invalid or junk input which you should handle gracefully without crashing. You may define additional methods but your total lines of code for Part 1 should not exceed 180 lines excluding comments.

In Part 2, you will extend your Apache to field HTTP/1.1 and FTP requests. You can use your browser to use Apache as a ‘proxy’; this is how we will test your Apache. Check with IA if you have questions on how to do this.

Apache should correctly service each request then close the connection. We will test with one request at a time. If errors occur it should close the connection, log ERROR and then proceed to the next request. No crash/hang.

Requests in Part 2 will be logged as follows:

(1) Incoming client connection from [A.B.C.D:nnnnn] to me [E.F.G.H:nnnnn]
    REQ: www.berkeley.edu/foo/something.txt (2234 bytes transferred)
HTTP GET requests for text / binary files up to 200MB should work correctly.

Apache should properly handle Full-Requests (RFC 1945 section 4.1) up to 65535 bytes. You may close the connection if a Full-Request is larger than that.

FTP requests will come inside an HTTP request like this:

GET ftp://ftp.univ.edu/pub/README.txt HTTP/1.1
Host: univ.edu
...
Apache must perform the FTP transaction using FTP passive mode with ftp.univ.edu and return the file, in an appropriate response, back to the client.

Apache must not have more than 20 Sockets open, must not use more than 20MB of heap memory. You should take care to free up resources after use. All requests must be completed in 20 seconds or less of Apache processing time. Excessive Apache spinning/spin-locking will attract penalties.

It is not necessary to support HTTPS or FTPS.
