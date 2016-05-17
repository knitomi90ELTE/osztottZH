package zh20160517_16.feladatB;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class KerdesGyujtemeny extends UnicastRemoteObject implements KerdesGyujtemenyInf {

    private List<Question> questions;

    public KerdesGyujtemeny() throws RemoteException {
        super();
    }
    
    @Override
    public void feltolt(String file) throws RemoteException {
        this.questions = readQuestions(file);
    }

    @Override
    public void ujKerdesValasz(String kerdes, int valasz) throws RemoteException {
        questions.add(new Question(kerdes, valasz));
    }

    @Override
    public String kovetkezoKerdesValasz() throws RemoteException {
        Question next = questions.get(0);
        questions.remove(next);
        return next.question + System.getProperty("line.separator") + next.answer;
    }
    
    private List<Question> readQuestions(String filename) {
        List<Question> q = new ArrayList<>();
        try (Reader reader = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(reader);
            while (br.ready()) {
                Question tmp = new Question();
                tmp.question = br.readLine();
                tmp.answer = Integer.parseInt(br.readLine());
                q.add(tmp); 
            }
        } catch (FileNotFoundException e) {
            System.out.println("No such file " + filename);
        } catch (IOException ex) {
            System.out.println("IOException while reading " + filename);
        }
        return q;
    }
    
}
