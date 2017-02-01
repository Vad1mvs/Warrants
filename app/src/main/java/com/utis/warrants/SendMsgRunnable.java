package com.utis.warrants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class SendMsgRunnable implements Runnable {
	public static final String DEST_IP = "192.168.2.238";
	public static final int UDP_PORT = 9090;

	public String msg;
	
	
	private void sendMsg(String Msg) {
		String udpMsg;
		DatagramSocket ds = null;
		
	    try {
	    	udpMsg = Msg;
	        ds = new DatagramSocket();
	        InetAddress serverAddr = InetAddress.getByName(DEST_IP);
	        DatagramPacket dp;
	        dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, UDP_PORT);
	        ds.send(dp);
	        Log.i("UDP packet sent to "+ serverAddr, udpMsg);

	    } catch (SocketException e) {
	        e.printStackTrace();
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }
	    }
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		sendMsg(msg);
	}

}
