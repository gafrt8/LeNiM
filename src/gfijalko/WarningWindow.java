package gfijalko;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WarningWindow extends WindowAdapter {

    private JFrame warningFrame;
    private ClickedToClose clickedToClose;

    WarningWindow(ClickedToClose clickedToClose) {
        this.clickedToClose = clickedToClose;

        JLabel infoLabel = new JLabel("Czy na pewno chcesz zakończyć?");
        JButton yesButton = new JButton("TAK");
        yesButton.addActionListener(new YesFinish());
        JButton noButton = new JButton("NIE");
        noButton.addActionListener(new NoFinish());

        JPanel framePanel = new JPanel();
        framePanel.add(infoLabel);
        framePanel.add(yesButton);
        framePanel.add(noButton);

        warningFrame = new JFrame("Warning");
        warningFrame.setLocation(800, 450);
        warningFrame.setSize(250, 100);
        warningFrame.setResizable(false);
        warningFrame.addWindowListener(new WindowAdapter() { // Akcja na zamknięcie
            @Override
            public void windowClosing(WindowEvent e) {
                warningFrame.setVisible(false);
                clickedToClose.setWindowEnabled(true);
            }
        });
        warningFrame.add(framePanel);
    }

    public void windowClosing(WindowEvent e) {
        clickedToClose.setWindowEnabled(false);
        warningFrame.setVisible(true);
    }

    /** Akcja przycisku "TAK" - zakończ */
    private class YesFinish implements ActionListener {
        /** Niszczy okno ostrzegawcze i kończy działanie okna wywołującego */
        public void actionPerformed(ActionEvent ae) {
            warningFrame.setVisible(false);
            warningFrame.dispose();
            clickedToClose.finish();
        }
    }

    /** Akcja przycisku "NIE" - nie kończ */
    private class NoFinish implements ActionListener {
        /** Przywraca stan sprzed kliknięcia */
        public void actionPerformed(ActionEvent ae) {
            warningFrame.setVisible(false);
            clickedToClose.setWindowEnabled(true);
        }
    }
}
