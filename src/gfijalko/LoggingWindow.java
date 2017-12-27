package gfijalko;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** View - okno logowania ( OL ) */

public class LoggingWindow {

    private Klient klient;
    private JFrame logFrame;
    private JLabel infoLabel;
    private JTextField login;
    /** Zlicza niepoprawne próby logowania */
    private int i = 0;

    LoggingWindow(Klient klient) {
        this.klient = klient;

        JLabel[] bar = new JLabel[3];
        for(int i=0; i<3; i++) {
            bar[i] = new JLabel();
            bar[i].setMaximumSize(new Dimension(10, 10));
        }

        infoLabel = new JLabel("Użyj od 3 do 10 znaków");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        login = new JTextField(10);
        login.setMaximumSize(new Dimension(110,22));
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton logButton = new JButton("Zaloguj");
        logButton.addActionListener(new LogButton());
        logButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel logPanel = new JPanel();
        Color color = new Color(255, 230, 204);
        logPanel.setBackground(color);
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.add(bar[0]);
        logPanel.add(infoLabel);
        logPanel.add(bar[1]);
        logPanel.add(login);
        logPanel.add(bar[2]);
        logPanel.add(logButton);

        logFrame = new JFrame("LOGOWANIE");
        logFrame.setLocation(350, 220);
        logFrame.setSize(350, 150);
        logFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // Docelowo: ostrzeżenie!
        logFrame.add(logPanel);
        logFrame.setResizable(false);
        logFrame.setVisible(true);
    }

    /** Akcja przycisku "Zaloguj" */
    private class LogButton implements ActionListener {
        /** Przekazuje wiadomość klientowi i wypisuje ją na dole konwersacji */
        public void actionPerformed(ActionEvent ae) {

            if(login.getText().equals("")) return; // Nie wysyłaj gdy brak tekstu

            Message mess = new Message(login.getText(), LetsGo.LOG_IN);
            klient.sendMess(mess); // Prześlij Klientowi treść wiadomości
        }
    }

    /** Zwraca login klienta */
    String getLogin() {
        return login.getText();
    }

    /** Zamyka logowanie */
    void loggingDone() {
        logFrame.setVisible(false);
    }

    /** Ponów logowanie */
    void tryAgain() {
        login.setText("");
        infoLabel.setText("Wybierz inny login (" + ++i + ").  Użyj od 3 do 10 znaków");
    }
}