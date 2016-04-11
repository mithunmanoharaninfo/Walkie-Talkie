package com.example.clientt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
 protected static final String TAG = "ImageButton Pressed";
			ImageButton b1;
			
		    String mFileName;
		    FileOutputStream mFileOut;
		    Socket mSocket;
		    ParcelFileDescriptor mPFD;
		    FileDescriptor mFD;
			int flag=0;
		MediaRecorder mRecorder;
	    MediaPlayer mPlayer;

	 static final int SocketServerPORT = 8080;
	 ClientRxThread clientRxThread;
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.activity_main);
	  mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
      mFileName += "/audiorecordtest.3gp";
	  b1=(ImageButton) findViewById(R.id.imageButton1);
		 b1.setOnTouchListener(mTalkTouch);
		clientRxThread =new ClientRxThread("192.168.43.67", SocketServerPORT);
		clientRxThread.execute();
	}
	 File file;

 class ClientRxThread extends AsyncTask<Void, Integer, Void> {
	  String dstAddress;
	  int dstPort;
	  public Thread t;
	  private String threadName="Client";
	   boolean suspended = false;
	  ClientRxThread(String address, int port) {
	   dstAddress = address;
	   dstPort = port;
	  }
	/*  public void start ()
	   {
		 
	      System.out.println("Starting " +  threadName );
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }*/
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		 Socket socket = null;
		  
		 while(true)
		   {
			 if(isCancelled())
                 break; 
		   try {
			  System.out.println("inside client");
		    socket = new Socket(dstAddress, dstPort);
		    file= new File(
		      Environment.getExternalStorageDirectory(), 
		      "audiorecordt.3gp");

		    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		    byte[] bytes;
		    FileOutputStream fos = null;
		    try {
		     bytes = (byte[])ois.readObject();
		     fos = new FileOutputStream(file);
		     fos.write(bytes);
		  
		   socket.close();
		    } catch (ClassNotFoundException e) {
		     // TODO Auto-generated catch block
		     e.printStackTrace();
		    }  finally {
		     if(fos!=null){
		      fos.close();
		     }
		     
		    } MainActivity.this.runOnUiThread(new Runnable() {

		     @Override
		     public void run() {
		      Toast.makeText(MainActivity.this, 
		        "Finished", 
		        Toast.LENGTH_LONG).show();
		      FileInputStream fileIn;
			try {
							fileIn = new FileInputStream(file);
				 mFD = fileIn.getFD();
				  
			      mPlayer = new MediaPlayer();
			      mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			      mPlayer.setDataSource(mFD);
			      mPlayer.prepare(); // might take long! (for buffering, etc)
			      mPlayer.start();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
		     }});
		    
		   } catch (IOException e) {

		    e.printStackTrace();
		    
		    final String eMsg = "Something wrong: " + e.getMessage();
		    MainActivity.this.runOnUiThread(new Runnable() {

		     @Override
		     public void run() {
		     Toast.makeText(MainActivity.this, 
		        eMsg, 
		        Toast.LENGTH_LONG).show();
		     }});
		    
		   } finally {
		    if(socket != null){
		     try {
		      socket.close();
		      clientRxThread =new ClientRxThread("192.168.43.67", SocketServerPORT);
				clientRxThread.execute();
		     } catch (IOException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		     }
		    }
		   }
		   
		  }
		return null;
		 
		
	}
	 
	  
	 }
	 
	 
	 
	 ///TOUCH WORKING CONDITION ARE APPLIED HERE
	 private OnTouchListener mTalkTouch = new OnTouchListener() {

	      @SuppressLint("ClickableViewAccessibility")
		public boolean onTouch(View yourButton, MotionEvent motion) {
	      
	        switch (motion.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	          /*Log.d(TAG, "Talk Down");*/ yourButton.setPressed(true);
	          try {
	        	clientRxThread.cancel(true);
	            startRecording();
	           
	          } catch (IllegalStateException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	          } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          break;
	        case MotionEvent.ACTION_UP:
	        	/*Log.d(TAG, "Talk Down");*/ yourButton.setPressed(false);
	          try {
	        	  stopRecording();
	        	  //System.out.println("this is it"+flag);
	       serverSocketThread = new ServerSocketThread();
			      	  serverSocketThread.execute();
			       } catch (IllegalArgumentException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	          } catch (IllegalStateException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	          } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          
	          break;
	        }
	        
	        return true;
	      }

		private void startRecording() throws IOException {
			// TODO Auto-generated method stub
			
		Toast.makeText(getApplicationContext(), "Recording...", Toast.LENGTH_SHORT).show();
			 mFileOut = new FileOutputStream(mFileName);
			  mFD = mFileOut.getFD();
			 mRecorder = new MediaRecorder();
		      mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		      mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		      mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		      mRecorder.setOutputFile(mFD);
		      mRecorder.prepare();
		      mRecorder.start(); 
		}

		private void stopRecording() throws IOException {
			// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Stopping...", Toast.LENGTH_SHORT).show();
			 mRecorder.stop();
		      mRecorder.release();
		      mRecorder = null;
		      mFileOut.close();
		      mFileOut = null;
		      

		}
	    };
	 //On Destroy 
	    
	    
	    
	    @Override
		 protected void onDestroy() {
		  super.onDestroy();
		  
		  if (serverSocket != null) {
		   try {
		    serverSocket.close();
		   } catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		   }
		  }
		 }
	    
	    
	    
	    
	    ///server programmm
	   
		 ServerSocket serverSocket;
		 
		 ServerSocketThread serverSocketThread;
	     class ServerSocketThread extends AsyncTask<Void, Integer, Void> {

	    	public Thread t;
	  	  private String threadName="Server";
	  	   boolean suspended = false;
	  	 
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub

				// TODO Auto-generated method stub
					 Socket socket = null;
					   
					   try {
						     serverSocket = new ServerSocket(SocketServerPORT);
					   while (true) 
					    {
						   if(isCancelled())
				                 break; 
					    //Toast.makeText(getApplicationContext(), "thread started", Toast.LENGTH_LONG).show();
					    System.out.println("Server thread");
					     socket = serverSocket.accept();
					     FileTxThread fileTxThread = new FileTxThread(socket);
					     fileTxThread.start();
					    	
					    }
					   
					   } catch (IOException e) {
					    // TODO Auto-generated catch block
					    e.printStackTrace();
					   } finally {
					    if (socket != null) {
					     try {
					      socket.close();
					     } catch (IOException e) {
					      // TODO Auto-generated catch block
					      e.printStackTrace();
					     }
					    }
					   }
				return null;
			}
		

		 }
		 
		 public class FileTxThread extends Thread {
		  Socket socket;
		  
		  FileTxThread(Socket socket){
		   this.socket= socket;
		  }

		  @Override
		  public void run() {
		   System.out.println("Server this is the sending of file");
		   File file = new File(
		     Environment.getExternalStorageDirectory(), 
		     "audiorecordtest.3gp");
		   
		   byte[] bytes = new byte[(int) file.length()];
		   BufferedInputStream bis;
		   try {
		    bis = new BufferedInputStream(new FileInputStream(file));
		    bis.read(bytes, 0, bytes.length);
		    
		    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); 
		    oos.writeObject(bytes);
		    oos.flush();
		    
		    socket.close();
		    
		    final String sentMsg = "File sent to: " + socket.getInetAddress();
		    MainActivity.this.runOnUiThread(new Runnable() {

		     @Override
		     public void run() {
		     Toast.makeText(MainActivity.this, 
		        sentMsg, 
		        Toast.LENGTH_LONG).show();
		     serverSocketThread.cancel(true);
		     }});
		 
		   } catch (FileNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		   } catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		   }  finally {
		    try {
		     socket.close();
		    
		    } catch (IOException e) {
		     // TODO Auto-generated catch block
		     e.printStackTrace();
		    }
		   }
		   
		  }
		 }
	 
		}