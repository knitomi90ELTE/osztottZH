package zh20160517_16.feladatB;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KerdesGyujtemenyInf extends Remote {

    void feltolt(String file) throws RemoteException;

    void ujKerdesValasz(String kerdes, int valasz) throws RemoteException;

    String kovetkezoKerdesValasz() throws RemoteException;

}
