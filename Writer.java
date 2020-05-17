import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Writer extends UnicastRemoteObject implements WriteInterface {
    private String nickname;
    protected ServerInterface serverInterface;
    Queue<String> receiveQueue = new LinkedList<>();
    Queue<String> sendQueue = new LinkedList<>();

    public Writer(String nickname, ServerInterface serverInterface)throws RemoteException{
        super();
        this.nickname = nickname;
        this.serverInterface = serverInterface;
    }
    
    public static void initialize(Writer writer) {
        try {
            Naming.rebind("rmi://localhost:5000/" + writer.nickname, writer);
        } catch(Exception e) {
            System.out.println("Failed initializing writer");
        }
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter nickname: ");
        String name = scan.next();
        Writer writer = null;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            ServerInterface serverInterface = (ServerInterface) Naming.lookup("rmi://localhost:5000/main");
            writer = new Writer(name, serverInterface);
        } catch (Exception e) {
            System.out.println("Failed initializing writer");
        }
        if(writer == null){
            System.out.println("Try again");
            scan.close();
            return;
        }
        initialize(writer);
        writer.registerWithServer(writer.nickname);
        Runnable writerComm = new DBWriter(writer.nickname, writer);
        executor.execute(writerComm);
        while(true){
            if(writer.sendQueue.size()>0){
                try {
                    String message = writer.sendQueue.remove();
                    if(message.equals("exit")){
                        break;
                    }
                    writer.serverInterface.sendMessage(writer.nickname, message );
                    
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            }
        }
        try{
            writer.serverInterface.disconnect(writer.nickname);
        } catch(Exception e) {
            System.out.println("Failed to leave chat");
        }
        scan.close();
        executor.shutdown();
        System.exit(0);
        
    }

    public void registerWithServer(String nickname) {
        try {
            serverInterface.connect(nickname);
        } catch(Exception e) {
            System.out.println("Failed connecting " + nickname);
        }
    }

    public void messageFromServer(String message) {
        System.out.println(message);
        receiveQueue.add(message);
    }

    public Queue<String> getReceiveQueue(){
        return receiveQueue;
    }
    public void addToSendQueue(String message){
        sendQueue.add(message);
    }

}

class DBWriter implements Runnable{
    String nickname;
    Writer writer;
    Queue <String> recQueue;
    Scanner scan = new Scanner(System.in);
    public DBWriter(String nName, Writer writer){
        this.nickname = nName;
        this.writer = writer;
    }
    

    public void run(){
        
            for (int i = 0; i < 10; i++) {
                writer.addToSendQueue("HEllp "+nickname);
            }
        
    }


    public void messageFromServer(String message) {
        System.out.println(message);
    }
} 