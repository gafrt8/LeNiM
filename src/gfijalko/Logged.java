package gfijalko;

import javax.swing.*;

public class Logged extends JButton {
    String conversation = null; // Potrzebne??
    String nick;
    /**
     * false - od rozmówcy, true - ode mnie (ostatnia wiadomość)
     */
    boolean czyJa;

    Logged(String nick) {
        setText(nick);
        this.nick = nick;
    }

    public void setCzyJa(boolean czyJa) {
        this.czyJa = czyJa;
    }
}

//    @Override
//    public void setText(String text) {
//        super.setText(text);
//        nick = text;
//    }