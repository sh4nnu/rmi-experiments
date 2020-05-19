import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Writer extends UnicastRemoteObject implements WriteInterface {
    private String nickname;
    protected ServerInterface serverInterface;
    boolean isSafe = true;
    Queue<String> receiveQueue = new LinkedList<>();
    Queue<String> sendQueue = new LinkedList<>();
    Queue<Student> sendStudentQueue = new LinkedList<>();

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
        if(message == "stop"){
            this.isSafe = false;
        }
        else{
            receiveQueue.add(message);
        }
    }

    public Queue<String> getReceiveQueue(){
        return receiveQueue;
    }
    public void addToSendQueue(String message){
        sendQueue.add(message);
    }
    public void addToStudentSendQueue(Student s){
        sendStudentQueue.add(s);
    }

    public boolean getIsSafe(){
        return this.isSafe;
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
        try{
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rmi"+nickname, "root", "asdf;lkj");
            Statement stmt = conn.createStatement();

            System.out.println("Writer ready");
            Random  rand = new Random();
            int t =0, x = 0, c = 0;
            boolean idExists = false;
            Thread.sleep(2000);
            String name = "Process "+nickname ;
            String branch = "cse";
            int percent = 01;
            String email = "mani.gmail";
         
            // while(writer.getIsSafe()){
                Student s = new Student();
	   	        t  = t% 7; 
                String query = "SELECT * FROM SAMPLERMI";
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()) {
                    if(t == rs.getInt("id")) {
                        idExists = true;
                        percent = rs.getInt("percentage")+5;
                        break;
                    }
                }
                s.setID(0); 
                s.setName(name); 
                s.setBranch(branch); 
                s.setPercent(percent); 
                s.setEmail(email); 
                s.setClock(c);
            //     switch(rand.nextInt(2)) {
            //         case 1:
            //             this.read(2, conn);
            //             //TODO: request write || add to write queue || write func                        
            //             break;

            //         default:
            //             System.out.println("NOTHING");
            //   }
            // }
            this.read(2, conn);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //Read from the database

    public Student read(int t, Connection con)throws Exception, ClassNotFoundException {
	      try {
 		      FileWriter logwtr = new FileWriter("Writers.log",true);
 		      BufferedWriter bw = new BufferedWriter(logwtr);
 		      PrintWriter pw = new PrintWriter(bw);
 		      System.out.println("Logging......");

 		      pw.println("P"+this.nickname+": Entry Read id: "+t);
 	          pw.flush();
 		      logwtr.close();
 		      
 		    } catch (IOException e) {
 		      System.out.println("An error occurred.");
 		      e.printStackTrace();
 		    }
	      // JDBC driver name and database URL 
	     
	      //Execute a query 
	      System.out.println("Creating statement..."); 
	      Statement stmt = con.createStatement();  
	      String sql = "SELECT * FROM samplermi where id ="+t; 
	      ResultSet rs = stmt.executeQuery(sql);  
	      //Extract data from result set
	         // Retrieve by column name

	      	if(rs.next()) {
	         int id  = rs.getInt("id"); 
	         
	         String name = rs.getString("name"); 
	         String branch = rs.getString("branch"); 
	         
	         int percent = rs.getInt("percentage"); 
	         String email = rs.getString("email");  
	         int clock = rs.getInt("clock");
			  con.close();

		      try {
	 		      FileWriter logwtr = new FileWriter("Writers.log",true);
	 		      BufferedWriter bw = new BufferedWriter(logwtr);
	 		      PrintWriter pw = new PrintWriter(bw);
	 		      System.out.println("LOGGING");
	 		      pw.println("P"+this.nickname+": Exit Read id: "+t+ " Percent: "+percent+" Clock: "+clock);
	 	          pw.flush();
	 		      logwtr.close();
	 		      
//	 		      System.out.println("Successfully wrote to the file.");
	 		    } catch (IOException e) {
	 		      System.out.println("An error occurred.");
	 		      e.printStackTrace();
	 		    }
	         // Setting the values 
	         Student st = new Student(); 
	         st.setID(id); 
	         st.setName(name); 
	         st.setBranch(branch); 
	         st.setPercent(percent); 
	         st.setEmail(email); 

	 		 return st;
	 		 
	      	}
		      try {
	 		      FileWriter logwtr = new FileWriter("Writers.log",true);
	 		      BufferedWriter bw = new BufferedWriter(logwtr);
	 		      PrintWriter pw = new PrintWriter(bw);
                   System.out.println("LOGGING....");
	 		      pw.println("P"+this.nickname+": Exit Read id: "+t+ " Percent: "+0+" Clock: "+0);
	 	          pw.flush();
	 		      logwtr.close();
	 		      
	 		    } catch (IOException e) {
	 		      System.out.println("An error occurred.");
	 		      e.printStackTrace();
				 }
	     return null;
    }
    //Write a student into its db.

    public void write(Student s,  Connection conn)throws Exception{
        //Execute a query 
	      System.out.println("Creating statement...");
	      
	      boolean idExists = false;
	      Statement stmt = conn.createStatement();
	      String sql = "SELECT * FROM samplermi"; 
	      ResultSet rs = stmt.executeQuery(sql);  
	      
	      int id = s.getId();
	      String name = s.getName();
	      String branch = s.getBranch();
	      int percent = s.getPercent();
	      String email = s.getEmail();
	      int clock = s.getClock();

	      int t = id % 7;
	      try {
 		      FileWriter logwtr = new FileWriter("Writers.log",true);
 		      BufferedWriter bw = new BufferedWriter(logwtr);
 		      PrintWriter pw = new PrintWriter(bw);
 		      System.out.println("Logging.....");

 		      pw.println("P"+this.nickname+": Entry Write id: "+t+" Percent: "+ percent);
 	          pw.flush();
 		      logwtr.close();
 		      
 		    } catch (IOException e) {
 		      System.out.println("An error occurred.");
 		      e.printStackTrace();
 		    }
	      // search for id in the database 
	      while(rs.next()) {
	    	  if(t == rs.getInt("id")) {
	    		  idExists = true;
	    		  break;
	    	  }
	      }
	      
          stmt = conn.createStatement();
          // If the corresponding  row is not there in the db , INSERT 
	      if (!idExists) {
		      String insert = "INSERT INTO samplermi(id, name, branch, percentage, email,clock) values('"+t+"','"+name+"','"+branch+"','"+percent+"','"+email+"',"+clock+")";
		      stmt.executeUpdate(insert);
	      }
	      else {
	    	  
	    	  String update = "UPDATE samplermi SET percentage = "+percent+", name = '"+name+"', clock = "+clock+" where id = "+t;
		      stmt.executeUpdate(update);
	      }
	      try {
 		      FileWriter logwtr = new FileWriter("Writers.log",true);
 		      BufferedWriter bw = new BufferedWriter(logwtr);
 		      PrintWriter pw = new PrintWriter(bw);
 		      System.out.println("Logging.....");

 		      pw.println("P"+this.nickname+": Exit Write id: "+t	 +" Percent: "+ percent+" Clock: "+clock);
 	          pw.flush();
 		      logwtr.close();
 		    } catch (IOException e) {
 		      System.out.println("An error occurred.");
 		      e.printStackTrace();
 		    }
	      conn.close();
      System.out.println("wrote in Writer"+this.nickname);
    }

    public void messageFromServer(String message) {
        System.out.println(message);
    }
} 