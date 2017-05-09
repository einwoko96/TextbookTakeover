package com.app.helper;

import com.app.socket.SocketIO;

import org.json.JSONObject;

import java.net.MalformedURLException;

import static com.app.utils.Constants.SOCKET_URL;


public class Chat extends Thread {

    private SocketIO socket;
    private ChatCallback callback;
    
    public Chat(){
    	
    }

    public Chat(ChatCallbackAdapter callback) {
        this.callback = new ChatCallback(callback);
    }

    @Override
    public void run() {
        try {
            /*try {
                SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }*/
            socket = new SocketIO(SOCKET_URL, callback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    public void sendMessage(String type, JSONObject jobj) {
        try {
          
            socket.emit(type, jobj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setTypingStatus(String type, JSONObject jobj) {
        try {

            socket.emit(type, jobj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void join(String type, JSONObject username) {
        try {
           
            String str[] = new String[2];
            str[0]=type;
            
            socket.emit(str[0], callback, username);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void checkRoom(String name){
    	try{
    		socket.emit("check", name);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void createRoom(String name){
    	try{
    		
    		socket.emit("createRoom", callback , name);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void leaveRoom(String name){
    	try{
    		socket.emit("leaveRoom", name);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void joinRoom(String id){
    	try{
    		
    		socket.emit("joinRoom", callback , id);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    public void disconnect(){
        try{
            socket.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
