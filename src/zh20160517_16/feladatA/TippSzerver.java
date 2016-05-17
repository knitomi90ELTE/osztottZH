package zh20160517_16.feladatA;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
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
        while (true) {
            try {
                for (int i = 0; i < clientCount; i++) {
                    clients.add(new InnerClient(server.accept()));
                    System.out.println("Client connected");
                }
                //System.out.println("Starting handler with " + clients.size() + " clients.");
                new Handler(new ArrayList<>(clients.subList(clients.size() - clientCount, clients.size()))).start();
            } catch (IOException ex) {
                System.out.println("Hiba a kliensek csatlakozasakor.");
                break;
            }
        }
    }

    private class Handler extends Thread {

        private final List<InnerClient> handlerClients;

        public Handler(List<InnerClient> clients) {
            this.handlerClients = clients;
        }

        @Override
        public void run() {
            while (true) {
                int myQuestion = currentQuestion;
                System.out.println(myQuestion + " " + handlerClients.size());
                for (InnerClient client : handlerClients) {
                    client.sendMessage(questions.get(myQuestion).question);
                    System.out.println("Kerdes elkuldve!");
                }
                currentQuestion++;
                if (currentQuestion == questions.size()) {
                    currentQuestion = 0;
                }
                //System.out.println(handlerClients.get(0).toString());
                //System.out.println(handlerClients.get(1).toString());
                for( Iterator< InnerClient > it = handlerClients.iterator(); it.hasNext() ; ){
                //for (InnerClient client : handlerClients) {
                    InnerClient client = it.next();
                    int answer = client.getMessage();
                    System.out.println("Valasz: " + answer);
                    client.setEpszilon(Math.abs(questions.get(myQuestion).answer) - answer);
                    String s;
                    if (answer < questions.get(myQuestion).answer) {
                        s = "kisebb";
                    } else if (answer > questions.get(myQuestion).answer) {
                        s = "nagyobb";
                    } else {
                        s = "helyes";
                    }
                    client.setDiff(s);
                }
                int max = 0;
                for (int i = 0; i < handlerClients.size(); i++) {
                    InnerClient c = handlerClients.get(i);
                    if (c.getEpszilon() > max) {
                        max = c.getEpszilon();
                    }
                }
                List<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < handlerClients.size(); i++) {
                    InnerClient c = handlerClients.get(i);
                    if (c.getEpszilon() == max && handlerClients.size() > 0) {
                        indexes.add(i);
                    }
                }
                for (int i = indexes.size()-1; i >=0 ; i--) {
                    int index = indexes.get(i);
                    InnerClient c = handlerClients.get(index);
                    c.sendMessage("Vesztettel.");
                    c.closeConnection();
                    handlerClients.remove(c);
                }
                
                if (handlerClients.size() > 1) {
                    for (InnerClient c : handlerClients) {
                        c.sendMessage("Jatekban maradtal, a tipped " + c.getDiff());
                    }
                } else {
                    handlerClients.get(0).sendMessage("Gyoztel.");
                    break;
                }

            }
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
            this.epszilon = epszilon;
        }

        public String getDiff() {
            return diff;
        }

        public void setDiff(String diff) {
            this.diff = diff;
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
