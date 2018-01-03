package gfijalko;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** View - okno servera */

public class ServerWindow {

//    private Server server;
    private JFrame serverFrame;
    private JTextArea statements;
    private JLabel[] nickLabel;
    private JLabel fill;

    ServerWindow(Server server) {
//        this.server = server;

        fill = new JLabel();

        JPanel nicksPanel = new JPanel();
        GridLayout nicksLayout = new GridLayout(LetsGo.LIMIT, 1);
        nicksPanel.setLayout(nicksLayout);
        nickLabel = new JLabel[LetsGo.LIMIT];
        for(int i=0; i<LetsGo.LIMIT; i++) {
            nickLabel[i] = new JLabel();
            nicksPanel.add(nickLabel[i]);
        }

        statements = new JTextArea();
        statements.setMargin(new Insets(10,10,10,10));
        statements.setEditable(false);
        statements.setLineWrap(true); // Zawijaj tekst
        statements.setWrapStyleWord(true); // Zawijaj tak, żeby słów nie cięło
        statements.setBackground(Color.black);
        statements.setBackground(statements.getDisabledTextColor());
        JScrollPane statementsPane = new JScrollPane(statements); // Żeby scrollować się dało

        JPanel framePanel = new JPanel();
        BorderLayout frameLayout = new BorderLayout();
        framePanel.setLayout(frameLayout);
        framePanel.add(fill, BorderLayout.PAGE_START);
        framePanel.add(statementsPane, BorderLayout.CENTER);
        framePanel.add(nicksPanel, BorderLayout.LINE_END);

        serverFrame = new JFrame("SERVER");
        serverFrame.setLocation(850, 120);
        serverFrame.setSize(550, 250);
        serverFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        serverFrame.addWindowListener(new WindowAdapter(){ // Ostrzeżenie przed zamknięciem
            public void windowClosing(WindowEvent e) {
                Object[] options = {"TAK", "NIE"};
                int n = JOptionPane.showOptionDialog(serverFrame,
                        "Czy jesteś pewien, że chcesz zakończyć?",
                        "WARNING",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,     //do not use a custom Icon
                        options,  //the titles of buttons
                        options[0]); //default button title
                if(n==JOptionPane.YES_OPTION) {
                    serverFrame.setVisible(false);
                    serverFrame.dispose();
                    server.finish();
                }
            }});
        serverFrame.add(framePanel);
        serverFrame.setVisible(true);
    }

    void Update(String[] nickList) {
        int i;
        for(i=0; nickList[i] != null; i++) {
            nickLabel[i].setText(nickList[i]);
        }
        nickLabel[i].setText(""); // Kasuj tekst następnego labela (potrzebne przy czyimś wylogowaniu)
        fill.setText("Ilość zalogowanych:  " + i + " /" + LetsGo.LIMIT);
    }

    void setStatements(String statement) {
        statements.setText(statements.getText() + "\n" + statement);
    }
}


//    @Override
//    public void setWindowEnabled(boolean b) {
//        serverFrame.setEnabled(b);
//    }
//
//    @Override
//    public void finish() {
//        serverFrame.setVisible(false);
//        serverFrame.dispose();
//        server.finish(); // Zakończ pracę servera
//        System.exit(0);
//    }