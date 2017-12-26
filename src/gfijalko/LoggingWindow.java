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

    LoggingWindow(Klient klient) {
        this.klient = klient;

        infoLabel = new JLabel("Użyj od 3 do 10 znaków");
        login = new JTextField();
        login.setSize(100,20);
        JButton logButton = new JButton("Zaloguj");
        logButton.addActionListener(new LogButton());

        JPanel logPanel = new JPanel();
        Color color = new Color(255, 230, 204);
        logPanel.setBackground(color);
        logPanel.add(infoLabel);
        logPanel.add(login);
        logPanel.add(logButton);

        logFrame = new JFrame("LOGOWANIE");
        logFrame.setLocation(350, 220);
        logFrame.setSize(350, 150);
        logFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Docelowo: ostrzeżenie!
        logFrame.add(logPanel);
        logFrame.setVisible(true);
    }

    /** Akcja przycisku "Zaloguj" */
    private class LogButton implements ActionListener {
        /** Przekazuje wiadomość klientowi i wypisuje ją na dole konwersacji */
        public void actionPerformed(ActionEvent ae) {

            if(login.getText().equals("")) return; // Nie wysyłaj gdy brak tekstu

            Message mess = new Message(login.getText(), LetsGo.logIn);
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
        infoLabel.setText("Wybierz inny login\nUżyj od 3 do 10 znaków");
    }
}