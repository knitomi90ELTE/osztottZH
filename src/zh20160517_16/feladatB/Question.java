package zh20160517_16.feladatB;

public class Question {

    public String question;
    public int answer;

    public Question() {
    }

    public Question(String question, int answer) {
        this.question = question;
        this.answer = answer;
    }
    
    @Override
    public String toString() {
        return question + " " + answer;
    }

}
