import java.rmi.*;

public interface WriteInterface extends Remote{
    public void messageFromServer(String message) throws RemoteException;
}