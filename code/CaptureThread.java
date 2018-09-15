/*
 *                       PacketEye (Packet Sniffer) application
 *                      Developed as mini project in RGUKT Nuzvid
 *            Developed by Ayyappa swamy, Rama Krishna, Vara Lakshmi and Mani
 *                               Completed in 07/04/2017
 */
 
 
 // This is a CaptureThread class that contains code that helps to simultaneously capturing and displaying the captured data.

import javax.swing.SwingUtilities;

abstract class CaptureThread {
	
	private Object value;
	
	//Nested class maintains reference to current thread under seperate sync control
	private static class ThreadVar{
		private Thread thread;
		ThreadVar(Thread t){
			thread = t;
		}
		synchronized Thread get(){
			return thread;
		}
		synchronized void clear(){
			thread = null;
		}
	}
	
	private ThreadVar threadVar;
	
	//Accessor methods
	protected synchronized Object getValue(){
		return value;
	}
	private synchronized void setValue(Object x){
		value = x;
	}
	
	//Compute value to be returned. Abstract so must be implemented
	public abstract Object construct();
	
	//Called on event dispatching thread (not on the worker thread)
	public void finished(){};
	
	//Method that interrupts worker thread. Force worker to stop
	public void interrupt(){
		Thread t = threadVar.get();
		if(t != null){
			t.interrupt();
		}
		threadVar.clear();
	}
	
	
	//Return value created by the constructing thread, null if constructing or current thread have been interrupted
	public Object get(){
		while(true){
			Thread t = threadVar.get();
			if(t==null)	return getValue();
			try{
				t.join();
			}
			catch (Exception e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}
	
	//Start a thread that will call constructor and exit
	public CaptureThread(){
		final Runnable doFinished = new Runnable(){
			public void run(){
				finished();
			}
		};
		
		Runnable doConstruct = new Runnable() {
			
			public void run() {
				try{
					setValue(construct());
				}
				finally{
					threadVar.clear();
				}
				
				SwingUtilities.invokeLater(doFinished);
			}
		};
		Thread t = new Thread(doConstruct);
		threadVar = new ThreadVar(t);
		
	}
	
	public void start(){
		Thread t = threadVar.get();
		if(t != null){
			t.start();
		}
	}

}
