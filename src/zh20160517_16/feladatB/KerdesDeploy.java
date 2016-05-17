package zh20160517_16.feladatB;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class KerdesDeploy {
    
    public static void main(String[] args) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(8888);
        registry.rebind("kerdesek", new KerdesGyujtemeny());
        System.out.println("Kerdesek létrehozva.");
    }
    
}
