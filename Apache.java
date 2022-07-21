/*
* CS 656 / Spring 21 / Apache Mediator / Stub V3.00
* Group: C4 / Leader: Patrick O'Keefe (pmo6)
* Group Members: Colton Johnson(CJ) (cj236), Darius Karoon (dk544), Harshal Bhole (hb32)
*
*   ADC - add your code here
*   NOC - do not change this
*   ??  - you may change these vars/parms etc
*   Your own methods must be after run() ONLY!
*/
import java.net.InetAddress;
// other imports go here
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.OutputStream;

/*--------------- end imports --------------*/

class Apache {

  // NOC these 3 fields
  private byte []     HOST ;	  /* should be initialized to 1024 bytes in the constructor */
  private int         PORT ;	  /* port this Apache should listen on, from cmdline        */
  private InetAddress PREFERRED ; /* must set this in dns() */
  // ADC additional fields here
  private int         dc;
  private int         endl;
//  private             FILE ;	  /* name of the file in URL, if you like */
  private byte []     BUFF ;      /* global buff for reference in echo_req */
  private boolean http;
  private boolean ftp;
  private final int DBUFFSIZE = 1;
  private byte[] path;
  private boolean errorOn = false;

public static void main(String [] a) { // NOC - do not change main()

  Apache apache = new Apache(Integer.parseInt(a[0]));

  apache.run(2);
}

Apache(int port) {
  PORT = port;
  // other init stuff ADC here
  HOST = new byte[1024];  
  BUFF = new byte[65536];
}

// Note: parse() must set HOST correctly
int parse(byte [] buffer) throws Exception
{
	
	byte[] b = new byte[6];
        for(int i = 0; i < buffer.length - 6; i++){
		boolean isEqual = true;
		for(int j = 0; j < 6; j++) b[j] = buffer[i + j];
		byte[] c = "Host: ".getBytes();
		for(int l = 0; l < 6; l++){if(b[l] != c[l]){isEqual = false; break;}}
		if(isEqual){
                       	int j = 0;
                       	int s = i + 6;
			while(!Character.isWhitespace((char)buffer[s + j])) j++;
			endl = j;
              	        for(int k = 0; k < j; k++) HOST[k] = buffer[s + k];
			break;
               	}
        }
	return 0;
}

// Note: dns() must set PREFERRED
int dns(int X)  // NOC - do not change this signature; X is whatever you want
{
	try
	{
        	InetAddress[] host = InetAddress.getAllByName(byte2str(HOST, 0, endl)); //Store all addresses of HOST into array
        	long goodDT = (long)Math.pow(2, ((8 * Long.BYTES) - 1)) - 1; //Set initial best time as largest possible positive value of long
        	for (int i = 0; i < host.length; i++) { byte[] na = host[i].getAddress();
                	if(na.length == 4) //Only proceed if IPv4
                	{
                		long start = System.currentTimeMillis(); //Start time
                        	InetAddress pt = host[i].getByName(host[i].getHostName()); //Use getByName to ping host
                        	long end = System.currentTimeMillis(); //End time
                        	long dt = end - start; //End time - Start time = RTT
	                        if(dt < goodDT) //If RTT better than best time previously set
        	                {
                	        	goodDT = dt; //Save new best time
                        	        PREFERRED = pt; //Save new preferred InetAddress
                        	}
                 	}
        	}
	}
	catch(UnknownHostException e)
	{
		return -1;
	}
	catch(Exception e)
	{
	}
	return 0;
}

int http_fetch(Socket client) // NOC - don't change signature
{
  int br = 0;
  OutputStream cOut;
  Socket p = new Socket(); // peer, connection to HOST
  try{
  	p = new Socket(PREFERRED, 80);
	cOut = client.getOutputStream();

  // get file from peer (HOST) and send back to c
  //
  	if(dc == 0)
  	{
		OutputStream out = p.getOutputStream();
        	InputStream in = p.getInputStream();
	        byte[] comm = new byte[DBUFFSIZE];
	        out.write(BUFF);
        	int r;
		for(;;)
       		{
                	r = in.read(comm);
                	cOut.write(comm);
			br += r;
                	if(in.available() == 0)
                        	break;
                	else
                    		comm = new byte[DBUFFSIZE];
        	}
	}
	else
		cOut.write("HTTP/1.1 503 SERVICE UNAVAILABLE\r\n".getBytes());
  }
  catch(UnknownHostException e)
  {
	errorOn = true;
	try
	{
		cOut = client.getOutputStream();
		cOut.write("HTTP/1.1 504 GATEWAY TIMEOUT\r\n".getBytes());
	}
	catch(Exception x)
	{
	}
  }
  catch(Exception ee)
  {
	  errorOn = true;
  }
  finally
  {
	  try
	  {
	  	p.close();
	  	client.close();
	  }
	  catch(Exception eee)
	  {
	  }
  }
  // return bytes transferred
  return br;
}

int  ftp_fetch(Socket client) // NOC - don't change signature
{
  int n = 0;
  Socket p = new Socket();  // peer, connection to HOST
  Socket downloadSocket = new Socket();
  try
  {
  	p = new Socket(PREFERRED, 21);
	OutputStream cOut = client.getOutputStream();
	if(dc != 0)
	{
		cOut.write("HTTP/1.1 503 SERVICE UNAVAILABLE\r\n".getBytes());
		return 0;
	}
  	OutputStream out = p.getOutputStream();
  	InputStream in = p.getInputStream();
  	byte[] comm = new byte[1024];
  	byte[] iBuff = new byte[1024];
  	byte[] i1 = new byte[1];
 	byte[] i2 = new byte[1];
  	in.read(iBuff);
  	if(iBuff[0] == 50 && iBuff[1] == 50 && iBuff[2] == 48)
  	{
  		comm = sendLine("USER anonymous\r\n".getBytes(), in, out);
		if(!(comm[0] == 51 && comm[1] == 51 && comm[2] == 49))
			throw new UnknownHostException();
        	comm = sendLine("PASS pmo6@njit.edu\r\n".getBytes(), in, out);
		if(!(comm[0] == 50 && comm[1] == 51 && comm[2] == 48))
                        throw new UnknownHostException();
        	comm = sendLine("PASV\r\n".getBytes(), in, out);
		if(!(comm[0] == 50 && comm[1] == 50 && comm[2] == 55))
                        throw new UnknownHostException();
        	int cc = 0;
        	for(int i = 0; i < comm.length - 1; i++)
        	{
        		byte t1 = comm[i];
                	if(t1 == (byte)',')
                	{
                	    cc++;
                	    if(cc == 4)
                	    {
                	        int ii = i + 1;
                	        for(int j = ii; j < comm.length - 1; j++)
                	        {
                	            byte t2 = comm[j];
                	            if(t2 == (byte)',')
                	            {
                	                i1 = new byte[(j - ii)];
                	                for(int k = ii; k < j; k++)
                	                {
                	                    i1[k - ii] = comm[k];
                	                }
                	                int jj = j + 1;
                	                for(int k = jj; k < comm.length; k++)
                	                {
                	                    byte t = comm[k];
                	                    if(t == (byte)')')
                	                    {
                	                        i2 = new byte[(k - jj)];
                	                        for(int m = jj; m < k; m++)
                	                        {
                	                            i2[m - jj] = comm[m];
                	                        }
                	                        break;
                	                    }
                	                }
                	                break;
                	            }
                	        }
                	        break;
                	    }
                	}
            	}	
            	for(int i = 0; i < i1.length; i++)
            	    i1[i] -= 48;
            	for(int i = 0; i < i2.length; i++)
            	    i2[i] -= 48;
            	int ii1 = 0;
            	int ii2 = 0;
            	for(int i = 0; i < i1.length; i++)
            	    ii1 += i1[i] * Math.pow(10, ((i1.length - 1) - i));
            	for(int i = 0; i < i2.length; i++)
            	    ii2 += i2[i] * Math.pow(10, ((i2.length - 1) - i));
            	ii1 *= 256;
            	int pp = ii1 + ii2;
            	downloadSocket = new Socket(PREFERRED, pp);
		byte[] dcc = new byte[path.length + 7];
		byte[] retr = "RETR ".getBytes();
		byte[] crlf = "\r\n".getBytes();
		for(int i = 0; i < retr.length; i++)
			dcc[i] = retr[i];
		for(int i = 0; i < path.length; i++)
			dcc[5 + i] = path[i];
		for(int i = 0; i < crlf.length; i++)
			dcc[5 + path.length + i] = crlf[i];
		comm = sendLine(dcc, in, out);
		if(!(comm[0] == 49 && comm[1] == 53 && comm[2] == 48))
                        throw new UnknownHostException();
            	byte[] file = new byte[DBUFFSIZE];
            	InputStream dIn = downloadSocket.getInputStream();
	    	int r = 0;
	    	cOut.write("HTTP/1.1 200 OK\r\n".getBytes());
	    	for(;;)
            	{
            		r = dIn.read(file);
			n += r;
        	        cOut.write(file);
        	        if(dIn.available() == 0)
        	        	break;
			else
 	        	        file = new byte[DBUFFSIZE];
		}
	}
	else
		throw new UnknownHostException();
  }
  catch(UnknownHostException e)
  {
	errorOn = true;
        try
        {
                OutputStream cOut = client.getOutputStream();
                cOut.write("HTTP/1.1 504 GATEWAY TIMEOUT\r\n".getBytes());
        }
        catch(Exception x)
        {
        }
  }
  catch(Exception ee)
  {
	  errorOn = true;
  }
  finally
  {
	  try
	  {
	  	downloadSocket.close();
	  	p.close();
	  	client.close();
	  }
	  catch(Exception eee)
	  {
	  }
  }
  // do	FTP transaction with peer, get file, send back to c
  // Note: do not 'store' the file locally; it must be sent
  // back as it arrives
  //	
  // return bytes transferred
  return n;
}

int  echo_req(Socket client) // NOC - don't change signature
{
   // used in Part 1 only; echo the HTTP req with added info
   // from PREFERRED
   //set preffered IP to byte array to write to client output stream
   try{
     //write to output stream original req, hostname, and preffered IP.
     OutputStream out = client.getOutputStream();
     byte [] pIP = PREFERRED.getHostAddress().getBytes();
     if(dc == 0)
     {
     	out.write("HTTP/1.1 200 OK\r\n".getBytes());
     	out.write(BUFF);
	out.write("<style>ar{color: #a00000;}</style>".getBytes());
     	out.write("\r\n\r\nDNS LOOKUP: ".getBytes());
	out.write(byte2str(HOST, 0, endl).getBytes());
     	out.write("\r\n<ar>Preferred IP: ".getBytes());
     	out.write(pIP);
     	out.write("</ar>\r\n".getBytes());
     	out.close(); 
     }
     else
     	out.write("HTTP/1.1 503\r\n".getBytes());
   }catch (IOException e) {
     e.printStackTrace();
   }
   return 0;
}

int run(int X)  // NOC - do not change the signature for run()
{
  ServerSocket s0 = null; // NOC - this is the listening socket
  Socket       s1 = null; // NOC - this is the accept-ed socket i.e. client
  byte []      b0 = new byte[65536] ;  // ADC - general purpose buffer
  int          cc = 0; //connection count

  // ADC here
  //Server socket connection 
  try{
    s0 = new ServerSocket();
    SocketAddress address = new InetSocketAddress(PORT);
    //Bind socket
    s0.bind(address, 50);
    System.out.println("Server listening on: " + PORT);

  }catch (UnknownHostException e) {
  }
  catch (IOException e) {
    e.printStackTrace();
  }
  catch(Exception e)
  {
  	e.printStackTrace();
  }


  while ( true ) {        // NOC - main loop, do not change this!
    // ADC from here to LOOPEND : add or change code
    errorOn = false;
    try{
      //accept incoming connection
      s1 = s0.accept();

      //Get bytes from connection and write them to b0
      InputStream in = s1.getInputStream();
      in.read(b0); // example: req is "GET http://site.com/dir1/dir2/file2.html"
      //Set BUFF global variable equal to b0 after bytes are read into b0
      BUFF = b0;
    }catch (IOException e) {
      e.printStackTrace();
    }
    try{
    	parse(b0); // set HOST as 's' 'i' 't' 'e' '.' 'c' 'o' 'm'
	dc = dns(0);
        // sets PREFERRED
        cc++;
        //echo_req( s1 );  // used in Part 1 only
        System.out.println("(" + cc + ") " + "Incoming client connection from " +
        PREFERRED.getHostAddress() + ':' + PORT + " to me " + s1.getInetAddress().getHostAddress() + ':' + s1.getPort());
        if(!part2parse(BUFF) || !(BUFF[0] == 71 && BUFF[1] == 69 && BUFF[2] == 84))
        {
                OutputStream out = s1.getOutputStream();
                out.write("HTTP/1.1 400 BAD REQUEST\r\n".getBytes());
                throw new UnknownHostException();
        }
	//if(dc == 0)
        //	System.out.println("REQ: " + PREFERRED.getHostName() + " / " + "RESP: " + PREFERRED.getHostAddress());
	//else
	//	System.out.println("REQ: " + PREFERRED.getHostName() + " / " + "RESP: ERROR");
    /* Part 2 - hints
    is it http_fetch or ftp_fetch ??
    nbytes = http_fetch(s1) or ftp_fetch(s1);
    LOG "REQ http://site.com/dir1/dir2/file2.html transferred nbytes"
    */
    int nbytes = 0;
    byte[] pUrl = parseUrl(BUFF);
    path = parsePath(BUFF);
    if(http)
	    nbytes = http_fetch(s1);
    else if(ftp)
	    nbytes = ftp_fetch(s1);
    char sipi = '\0';
    if(path.length > 0)
	    sipi = '/';
    if(dc != 0 || errorOn)
	    throw new UnknownHostException();
    System.out.println("REQ: " + byte2str(HOST, 0, endl) + sipi + byte2str(path, 0, path.length) + " (" + nbytes + " bytes transferred)");
    }
    catch(UnknownHostException e)
    {
	    System.out.println("REQ: " + byte2str(HOST, 0, endl) + " / " + "RESP: ERROR");
    }
    catch(Exception e)
    {
    }
    //close connection
    finally
    {
    	try{
    		s1.close();
	}
	catch(Exception e)
	{
	}
    }

    // LOOPEND
  } // NOC - main loop
}

/* ------------- your own methods below this line ONLY ----- */

/* ------------- your own methods below this line ONLY ----- */
String byte2str(byte []b, int i, int j)
{
  // convert b[i] to b[j] to String and return it,
  // for example if b = "happy" and i=2 and j=4 then "app" is returned
  // may assume US ASCII
	char tempArray[] = new char[j];
	for (i = 0; i < j; i++){
		 tempArray[i] = (char)b[i];
	}
	return String.valueOf(tempArray);
}
// start at position i, and end at j




boolean part2parse(byte[] buffer) {
    byte[] b = new byte[4];
    for(int i = 0; i < buffer.length - 4; i++){
        boolean isEqualFtp = true;
        boolean isEqualHttp = true;

        for(int j = 0; j < 4; j++)
            b[j] = buffer[i + j];

            byte[] c = "ftp:".getBytes();
            byte[] h = "HTTP:".getBytes();
            for(int l = 0; l < 4; l++){
                if(b[l] != c[l]){
                    isEqualFtp = false; break;
                }
            }
            if(isEqualFtp){
                //return or call ftp fetch method
                ftp = true;
		http = false;
                return ftp;
            }

            for(int l = 0; l < 4; l++){
                if(b[l] != h[l]){
                    isEqualHttp = false; break;
                }
            }
            if(isEqualHttp){
                //return or call http fetch method
                http = true;
		ftp = false;
                return http;
            }
    }
    return false;

}
byte[] sendLine(byte[] inp, InputStream in, OutputStream out) throws IOException
{
	out.write(inp);
        byte[] buff = new byte[1024];
        in.read(buff);
        return buff;
}

byte[] parseUrl(byte[] buffer) throws IOException
{
	int j = 0;
        int s = 4;
        while(!Character.isWhitespace((char)buffer[s + j])) j++;
	byte[] URL = new byte[j];
        for(int k = 0; k < j; k++) URL[k] = buffer[s + k];
        return URL;
}

byte[] parsePath(byte [] buffer) throws IOException
{
        int hnLen = endl;
        byte[] b = new byte[hnLen];
	byte[] ret = new byte[1];
        for(int i = 0; i < buffer.length - hnLen; i++){
                boolean isEqual = true;
                for(int j = 0; j < hnLen; j++) b[j] = buffer[i + j];
                byte[] c = byte2str(HOST, 0, endl).getBytes();
                for(int l = 0; l < hnLen; l++){if(b[l] != c[l]){isEqual = false; break;}}
                if(isEqual){
                        int j = 0;
                        int s = i + hnLen + 1;
                        while(!Character.isWhitespace((char)buffer[s + j])) j++;
                        ret = new byte[j];
                        for(int k = 0; k < j; k++) ret[k] = buffer[s + k];
                        break;
                }
        }
	return ret;
}


} // class Apache
