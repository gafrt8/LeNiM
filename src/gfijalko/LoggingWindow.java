package gfijalko;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** View - okno logowania ( OL ) */

public class LoggingWindow {

    /**
     * Referencja do klienta
     */
    private Klient klient;
    /**
     * Ramka okna logowania
     */
    private JFrame logFrame;
    /**
     * Napis informacyjny
     */
    private JLabel infoLabel;
    /**
     * Pole tekstowe do wpisania nicku
     */
    private JTextField login;
    /**
     * Zlicza niepoprawne próby logowania
     */
    private int i = 0;
    /**
     * Warunki poprawnego nicku
     */
    private String conditions = "Użyj od 3 do 12 znaków";

    /**
     * Tworzy okno logowania
     */
    LoggingWindow(Klient klient) {
        this.klient = klient;

        JLabel[] bar = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            bar[i] = new JLabel();
            bar[i].setMaximumSize(new Dimension(10, 10));
        }

        infoLabel = new JLabel(conditions);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        login = new JTextField(10);
        login.setMaximumSize(new Dimension(110, 22));
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
        logFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        logFrame.addWindowListener(new WindowAdapter() { // Ostrzeżenie przed zamknięciem
            public void windowClosing(WindowEvent e) {
                Object[] options = {"TAK", "NIE"};
                int n = JOptionPane.showOptionDialog(logFrame,
                        "Czy jesteś pewien, że chcesz zakończyć?",
                        "WARNING",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,     //do not use a custom Icon
                        options,  //the titles of buttons
                        options[0]); //default button title
                if (n == JOptionPane.YES_OPTION) {
                    logFrame.setVisible(false);
                    logFrame.dispose();
                    klient.finish();
                    System.exit(0);
                }
            }
        });
        logFrame.add(logPanel);
        logFrame.setResizable(false);
        logFrame.setVisible(true);
    }

    /**
     * Akcja przycisku "Zaloguj"
     */
    private class LogButton implements ActionListener {
        /**
         * Przekazuje wpisany nick (login) do Servera
         */
        public void actionPerformed(ActionEvent ae) {
            if (login.getText().equals("")) return; // Nie wysyłaj gdy brak tekstu
            klient.sendMess(new Message(login.getText()));
        }
    }

    /**
     * Zwraca login klienta
     */
    String getLogin() {
        return login.getText();
    }

    /**
     * Zamyka logowanie (przyjęto login)
     */
    void setLoggingVisible(boolean b) {
        logFrame.setVisible(b);
        if (b) { // b==true oznacza ponowne logowanie
            login.setText("");
            infoLabel.setText(conditions);
        }
    }

    /**
     * Ponów logowanie (odrzucono login)
     */
    void tryAgain() {
        login.setText("");
        infoLabel.setText("Wybierz inny login (" + ++i + ").  " + conditions);
    }
}


//    @Override
//    public void setWindowEnabled(boolean b) {
//        logFrame.setEnabled(b);
//    }
//
//    @Override
//    public void finish() {
//        logFrame.setVisible(false);
//        logFrame.dispose();
//        klient.finish();
//        System.exit(0);
//    }