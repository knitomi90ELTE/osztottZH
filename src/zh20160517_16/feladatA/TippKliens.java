package zh20160517_16.feladatA;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TippKliens {

    private final Socket client;
    private final PrintWriter pw;
    private final Scanner serverOutput;
    private Scanner userInput;
    private List<Integer> answers;
    private boolean clientInput = true;
    
    public TippKliens(String[] args) throws IOException {
        
        int port = Integer.parseInt(args[0]);
        client = new Socket("localhost", port);
        pw = new PrintWriter(client.getOutputStream(), true);
        serverOutput = new Scanner(client.getInputStream());
        if (args.length == 1) {
            userInput = new Scanner(System.in);
        } else {
            clientInput = false;
            answers = new ArrayList<>();
            for(int i = 1; i < args.length; i++){
                answers.add(Integer.parseInt(args[i]));
            }
        }

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (process() == 0) {
                        break;
                    }
                }
            }
        }.start();
    }

    private int process() {
        int status = 1;
        String fromServer;
        try {
            fromServer = serverOutput.nextLine();
        } catch (Exception e) {
            //Ez szépen lekezeli, ha a szerver leállt.
            System.out.println("Hiba az uzenet olvasasakor.");
            return 0;
        }
        Pattern p = Pattern.compile("Jatekban");
        Matcher m = p.matcher(fromServer);
        if ("Gyoztel.".equals(fromServer)) {
            System.out.println("Gyoztem!");
            status = 0;
        } else if ("Vesztettel.".equals(fromServer)) {
            System.out.println("Vesztettem!");
            status = 0;
        } else if (m.find()) {
            System.out.println("Szerver valasza: " + fromServer);
        } else {
            System.out.println("Kerdes: " + fromServer);
            String answer;
            if(clientInput){
                answer = userInput.nextLine();
            } else {
                int randomNum = 0 + (int)(Math.random() * answers.size()); 
                answer = Integer.toString(answers.get(randomNum));
            }
            pw.println(answer);
        }
        return status;
    }

    public static void main(String[] args) throws IOException {
        
        //Ha a kliens kapott parancssori paramétereket (a portszám után), 
        //azokat küldje el sorban válaszokként a szervernek.
        new TippKliens(args);
    }

}
