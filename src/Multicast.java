import java.net.*;
import java.io.*;
import java.util.*; 
//import com.fazecast.jSerialComm.*; 
public class Multicast
{
	private static final String TERMINATE = "Exit";
	static String name;
	static volatile boolean finished = false;
	public static void main(String[] args)
	
	{
		//SerialPort puerto = SerialPort.getCommPort("COM3");
		// Creamos nuestro socket servidor 
		//ServerSocket server; 
		//Socket single_socket; 
		//int puerto = 9090; 
		//DataOutputStream salida; 
		//DataInputStream entrada; 
		
		if (args.length != 2)
			System.out.println("Two arguments required: <multicast-host> <port-number>");
		else
		{
			try
			{
				InetAddress group = InetAddress.getByName(args[0]);
				int port = Integer.parseInt(args[1]);
				Scanner sc = new Scanner(System.in);
				System.out.print("Enter your name: ");
				name = sc.nextLine();
				MulticastSocket socket = new MulticastSocket(port);
			
				// Since we are deploying
				socket.setTimeToLive(0);
				//this on localhost only (For a subnet set it as 1)
				
				socket.joinGroup(group);
				Thread t = new Thread(new
				ReadThread(socket,group,port));
			
				// Spawn a thread for reading messages
				t.start();
				
				// sent to the current group
				System.out.println("Start typing messages...\n");
				while(true)
				{
					//puerto.openPort(); //Abrimos el puerto 
					String message;
					message = sc.nextLine();
					if(message.equalsIgnoreCase(Multicast.TERMINATE))
					{
						finished = true;
						socket.leaveGroup(group);
						socket.close();
						break;
					}
					/*if(puerto.isOpen()) {
						String name = puerto.getSystemPortName();// tomamos el nombre del puerto 
						puerto.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
						InputStream in = puerto.getInputStream(); // leemos lo que exista en el puerto com
						BufferedReader br = new BufferedReader(new InputStreamReader(in)); //creamos un bufer del inputStream
						String read; 
						StringBuilder sb = new StringBuilder(); //creamos stringBuilder para crear un string a partir del bufer 
						while((read = br.readLine()) != null) {
							sb.append(read); 
						}
						message = sb.toString(); 
						
						message = name + ": " + message; //Creamos el mensaje con el nombre del puerto y lo enviado por COM 
						byte[] buffer = message.getBytes();
						DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,group,port);
						socket.send(datagram);
						

					}
					else {
						System.out.println("Puerto cerrado");
					}*/
					message = name + ": " + message;
					byte[] buffer = message.getBytes();
					DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,group,port);
					socket.send(datagram);
					
				}
			}
			catch(SocketException se)
			{
				System.out.println("Error creating socket");
				se.printStackTrace();
			}
			catch(IOException ie)
			{
				System.out.println("Error reading/writing from/to socket");
				ie.printStackTrace();
			}
		}
	}
}
class ReadThread implements Runnable
{
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private static final int MAX_LEN = 1000;
	ReadThread(MulticastSocket socket,InetAddress group,int port)
	{
		this.socket = socket;
		this.group = group;
		this.port = port;
	}
	
	@Override
	public void run()
	{
		while(!Multicast.finished)
		{
				byte[] buffer = new byte[ReadThread.MAX_LEN];
				DatagramPacket datagram = new
				DatagramPacket(buffer,buffer.length,group,port);
				String message;
			try
			{
				socket.receive(datagram);
				message = new
				String(buffer,0,datagram.getLength(),"UTF-8");
				if(!message.startsWith(Multicast.name))
					System.out.println(message);
			}
			catch(IOException e)
			{
				System.out.println("Socket closed!");
			}
			
		}
	}
}
