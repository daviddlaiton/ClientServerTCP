package Logica;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor extends Thread {

	public final static int PORT = 18798; 
	public final static int MAX_CLIENTS = 10;
	private Socket socketcliente;
	public static Servidor [] ts = new Servidor[MAX_CLIENTS];
	private int id =0;
	
    public static final int BUFFER_SIZE = 65000;
	public final static int MESSAGE_SIZE = 65000;
	public static final long MAX_TIME = 30000;
	
	public Servidor (Socket socketcliente, int id){
		this.socketcliente = socketcliente;
		this.id = id;

	}
	public static void main (String [] args ) throws IOException {

		int a,b = 1;
		b++;
		ServerSocket servsock = new ServerSocket(PORT);
		int numThreads = 0;
		while (true && numThreads < MAX_CLIENTS) {
			System.out.println("Escuchando en PORT "+PORT);
			Socket socketcliente0 = servsock.accept();
			numThreads ++;
			System.out.println("Conectado: " + socketcliente0.getInetAddress());

			socketcliente0.setReceiveBufferSize(BUFFER_SIZE);
			socketcliente0.setSendBufferSize(MESSAGE_SIZE);
			int i = 0;
			Servidor nuevo = new Servidor(socketcliente0,i++);
			ts[numThreads-1] = nuevo;
			nuevo.start();

		}

	}

	@Override
	public void run()
	{   
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(socketcliente.getInputStream()));
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socketcliente.getOutputStream()));
			while(true) {
				System.out.println("Thread "+id+": Esperando ruta");
				long transcurrido = 0;
				while(dis.available()<1 && transcurrido <= MAX_TIME) {
					sleep(1000);
					transcurrido += 1000;
					System.err.println(transcurrido);
				}
				if(transcurrido > MAX_TIME) {
					System.out.println("Thread "+id+": Conexión terminada por tiempo máximo de espera superado.");
					socketcliente.close();
					return;
				}
				String ruta = dis.readUTF();
				System.out.println(ruta);
				File myFile = new File (ruta);
				byte [] mybytearray  = new byte [(int)myFile.length()];
				FileInputStream fis = new FileInputStream(myFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				int n = 0;
				dos.writeInt((int)myFile.length());
				dos.flush();
				while((n = fis.read(mybytearray)) != -1)
				{
					dos.write(mybytearray, 0, n);
					dos.flush();
				}
				System.out.println("Thread "+id+": Enviando archivo " + myFile.getName() + "(" + mybytearray.length + " bytes)");
				bis.close();
				fis.close();
				System.out.println("Thread "+id+": Archivo transferido.");
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}