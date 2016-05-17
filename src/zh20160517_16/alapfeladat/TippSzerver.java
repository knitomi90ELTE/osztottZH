package zh20160517_16.alapfeladat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TippSzerver {

    private ServerSocket server;
    //Amennyiben egy játék során elfogynak a kérdések, kezdje elölről őket.
    private List<Question> questions;
    private int clientCount;
    private int currentQuestion = 0;

    public TippSzerver(int port, int n, String filename) {
        try {
            server = new ServerSocket(port);
            questions = readQuestions(filename);
            clientCount = n;
            System.out.println("Szerver elindult a " + port + " porton, " + n + " kliensre varok.");
        } catch (IOException ex) {
            System.out.println("Hiba a szerver inditasakor!");
        }
    }

    public void handleClients() {
        List<InnerClient> clients = new ArrayList<>();
        try {
            for (int i = 0; i < clientCount; i++) {
                clients.add(new InnerClient(server.accept()));
            }
        } catch (IOException ex) {
            System.out.println("Hiba a kliensek csatlakozasakor.");
        }
        while (true) {
            System.out.println(currentQuestion);
            for (InnerClient client : clients) {
                client.sendMessage(questions.get(currentQuestion).question);
                System.out.println("Kerdes elkuldve!");
            }
            for (InnerClient client : clients) {
                int answer = client.getMessage();
                System.out.println("Valasz: " + answer);
                client.setEpszilon(Math.abs(questions.get(currentQuestion).answer) - answer);
                String s;
                if (answer < questions.get(currentQuestion).answer) {
                    s = "kisebb";
                } else if (answer > questions.get(currentQuestion).answer) {
                    s = "nagyobb";
                } else {
                    s = "helyes";
                }
                client.setDiff(s);
            }
            int max = 0;
            for (int i = 0; i < clients.size(); i++) {
                InnerClient c = clients.get(i);
                if (c.getEpszilon() > max) {
                    max = c.getEpszilon();
                }
            }
            System.out.println("MAX: " + max);
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < clients.size(); i++) {
                InnerClient c = clients.get(i);
                if (c.getEpszilon() == max && clients.size() > 0) {
                    indexes.add(i);
                }
            }
            
            for (int i = indexes.size()-1; i >=0 ; i--) {
                int index = indexes.get(i);
                InnerClient c = clients.get(index);
                c.sendMessage("Vesztettel.");
                c.closeConnection();
                clients.remove(c);
            }
            
            if (clients.size() > 1) {
                for (InnerClient c : clients) {
                    c.sendMessage("Jatekban maradtal, a tipped " + c.getDiff());
                }
            } else {
                clients.get(0).sendMessage("Gyoztel.");
                break;
            }

            currentQuestion++;
            currentQuestion %= questions.size();
        }

    }

    private class InnerClient {

        private final Socket client;
        private final PrintWriter pw;
        private final Scanner sc;
        private int epszilon = 0;
        private String diff;

        public InnerClient(Socket client) throws IOException {
            this.client = client;
            pw = new PrintWriter(client.getOutputStream(), true);
            sc = new Scanner(client.getInputStream());
        }

        public void sendMessage(String s) {
            pw.println(s);
        }

        public int getMessage() {
            return Integer.parseInt(sc.nextLine());
        }

        public void closeConnection() {
            try {
                client.close();
            } catch (IOException ex) {
                System.out.println("Hiba a kapcsolat bezarasakor.");
            }
        }

        public int getEpszilon() {
            return epszilon;
        }

        public void setEpszilon(int epszilon) {
            System.out.println("epszilon: " + epszilon);
            this.epszilon = epszilon;
        }

        public String getDiff() {
            return diff;
        }

        public void setDiff(String diff) {
            this.diff = diff;
        }

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

    private class Question {

        public String question;
        public int answer;

        @Override
        public String toString() {
            return question + " " + answer;
        }

    }

    //TippSzerver kapjon három parancssori paramétert: egy portszámot, egy számot (n) és egy fájlnevet.
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int n = Integer.parseInt(args[1]);
        String filename = args[2];
        TippSzerver tsz = new TippSzerver(port, n, filename);
        tsz.handleClients();
    }

}
