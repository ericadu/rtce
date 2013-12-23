package main;

import rtceserver.RTCEServer;

/**
 * Runs the RTCE Server. 
 * @author Erica
 *
 */
public class Server {
	/**
	 * Start a rtce server.
	 */
    
    private static final int DEFAULT_PORT = 4444;
    
	public static void main(String[] args) {
	    int port;
		try{
		    if (args.length == 0){
		        port = DEFAULT_PORT;
		    }
		    else {
		        port = Integer.parseInt(args[0]);
    		} 
    			
		    RTCEServer server = new RTCEServer(port);
		    server.serve();
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
