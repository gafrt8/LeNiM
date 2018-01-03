package gfijalko;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** View - Główne Okno Programu ( GOP ) */

public class View {

    /** Referencja do klienta */
    private Klient klient;
    /** Ramka GOP'u */
    private JFrame frame;
    /** Panel przycików zalogowanych */
    private JPanel loggedPanel;
    /** Przyciski: wyślij, opcje */
    private JButton send, options;
    /** Napisy: aktualny stan (w sensie rozmówcy), twój nick */
    private JLabel currentStateLabel, meLabel, loggedLabel;
    /** Pola tekstowe: messageContent i conversation */
    private JTextArea messageContent, conv;
    /** Aktualny rozmówca */
    private Logged currentPerson;
    /** Lista zalogowanych klientów */
    private ArrayList <Logged> list;
    /** true -> brak połączenia z serverem */
    private boolean serverDown;
    /** Licznik zalogowanych klientów */
    private int numberOfLogged = 0;

    /** Konstruktor - buduje wygląd GOP'u */
    public View(Klient klient) {

        this.klient = klient; // Przypisanie referencji do klienta
        klient.refToView(this); // Przesłanie klientowi referencji do siebie
        list = new ArrayList<>(); // Tworzy listę zalogowanych

/*(PAGE_START)*/
        meLabel = new JLabel();
/*(PAGE_START)*/

/*konwersacja (LINE_START)*/
        JPanel jPaLS = new JPanel();
        BorderLayout bLyLS = new BorderLayout();
        jPaLS.setLayout(bLyLS);
        jPaLS.setBackground(Color.lightGray);

        JLabel konLabel = new JLabel("**KONWERSACJA**");
        konLabel.setHorizontalAlignment(JLabel.CENTER);
        jPaLS.add(konLabel, BorderLayout.PAGE_START);

        conv = new JTextArea(0, 15);
        conv.setMargin(new Insets(5,5,5,5));
        conv.setLineWrap(true); // Zawijaj tekst
        conv.setWrapStyleWord(true); // Zawijaj tak, żeby słów nie cięło
        conv.setEditable(false);
        conv.setBackground(Color.orange);
        JScrollPane jSPc = new JScrollPane(conv); // Żeby scrollować się dało
        jPaLS.add(jSPc, BorderLayout.CENTER);
/*konwersacja (LINE_START)*/

/*Treść wiadomości (CENTER)*/
        messageContent = new JTextArea();
        messageContent.setMargin(new Insets(10,10,10,10));
        messageContent.setLineWrap(true); // Zawijaj tekst
        messageContent.setWrapStyleWord(true); // Zawijaj tak, żeby słów nie cięło
        messageContent.setBackground(Color.cyan);
        JScrollPane jSPm = new JScrollPane(messageContent); // Żeby scrollować się dało
/*Treść wiadomości (CENTER)*/

/*lista klientów (LINE_END)*/
        JPanel jPaLE = new JPanel();
        BorderLayout bLyLE = new BorderLayout();
        jPaLE.setLayout(bLyLE);
        jPaLE.setBackground(Color.lightGray);

        loggedLabel = new JLabel();
        loggedLabel.setHorizontalAlignment(JLabel.CENTER);
        jPaLE.add(loggedLabel, BorderLayout.PAGE_START);

        GridLayout gLyLE = new GridLayout(LetsGo.LIMIT, 1);
        loggedPanel = new JPanel();
        loggedPanel.setLayout(gLyLE);
        jPaLE.add(loggedPanel, BorderLayout.CENTER);

//        logged = new Logged[LetsGo.limit]; // Tablica przycisków ludzi zalogowanych
//        for(int i=0; i<LetsGo.limit; i++) {
//            logged[i] = new Logged();
//            logged[i].setEnabled(false);
//            jPaLE.add(logged[i]);
//        }
/*lista klientów (LINE_END)*/

/*dolny kontener (PAGE_END)*/
        JPanel jPaPE = new JPanel();
        GridLayout gLyPE = new GridLayout(1, 3);
        jPaPE.setLayout(gLyPE);
        jPaPE.setBackground(Color.lightGray);

        options = new JButton("WYLOGUJ");
        options.addActionListener(new logOutAction());
        jPaPE.add(options);

        currentStateLabel = new JLabel();
        currentStateLabel.setHorizontalAlignment(JLabel.CENTER);
        jPaPE.add(currentStateLabel);

        send = new JButton("WYŚLIJ");
        send.setEnabled(false);
        send.addActionListener(new sendAction());
        jPaPE.add(send);
/*dolny kontener (PAGE_END)*/

/*cała ramka*/
        JPanel framePanel = new JPanel();
        BorderLayout bLyF = new BorderLayout(); // Układ GOP'u
        bLyF.setHgap(5);
        bLyF.setVgap(5);
        framePanel.setLayout(bLyF);
        Color color = new Color(255, 230, 204);
        framePanel.setBackground(color);

        framePanel.add(meLabel, BorderLayout.PAGE_START);
        framePanel.add(jPaLS, BorderLayout.LINE_START);
        framePanel.add(jSPm, BorderLayout.CENTER);
        framePanel.add(jPaLE, BorderLayout.LINE_END);
        framePanel.add(jPaPE, BorderLayout.PAGE_END);

        frame = new JFrame("LeNiM");
        frame.setLocation(300, 200);
        frame.setSize(550, 300);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter(){ // Ostrzeżenie przed zamknięciem
            public void windowClosing(WindowEvent e) {
                Object[] options = {"TAK", "NIE"};
                int n = JOptionPane.showOptionDialog(frame,
                        "Czy jesteś pewien, że chcesz zakończyć?",
                        "WARNING",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,     //do not use a custom Icon
                        options,  //the titles of buttons
                        options[0]); //default button title
                if(n == JOptionPane.YES_OPTION) {
                    frame.setVisible(false);
                    frame.dispose();
                    if(!serverDown) klient.finish();
                    else System.exit(0);
                }
        }});
        frame.add(framePanel);
/*cała ramka*/
    }

    /** Akcja przycisku "WYŚLIJ" */
    private class sendAction implements ActionListener {
        /** Przekazuje wiadomość klientowi i wypisuje ją na dole konwersacji */
        public void actionPerformed(ActionEvent ae) {

            if(messageContent.getText().equals("")) return; // Nie wysyłaj gdy brak tekstu

            Message mess = new Message(messageContent.getText(), klient.getMe(), currentPerson.nick);
            klient.sendMess(mess); // Prześlij Klientowi treść wiadomości

            // Dopisz do konwersacji:
            if(currentPerson.conversation != null) { // Jak coś już jest, to po odstępach
                if (currentPerson.czyJa) // Jeśli ostatnio napisałem ja
                    currentPerson.conversation += "\n" + messageContent.getText();
                else
                    currentPerson.conversation += "\n\n@ja\n" + messageContent.getText();
            }
            else
                currentPerson.conversation = "@ja\n" + messageContent.getText();

            conv.setText(currentPerson.conversation);
            currentPerson.setCzyJa(true);
            messageContent.setText(""); // Czyść messageContent
        }
    }

    /** Akcja przycisku "WYLOGUJ" */
    private class logOutAction implements ActionListener {
        /** Przygotowuje do zalogowania ponownie */
        public void actionPerformed(ActionEvent ae) {
            Object[] options = {"TAK", "NIE"};
            int n = JOptionPane.showOptionDialog(frame,
                    "Czy jesteś pewien, że chcesz się wylogować?",
                    "WARNING",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,     //do not use a custom Icon
                    options,  //the titles of buttons
                    options[0]); //default button title
            if (n == JOptionPane.YES_OPTION) {
                frame.setVisible(false);
                send.setEnabled(false);
                conv.setText("");
                list = new ArrayList<>(); // Utworzenie nowej listy zalogowanych
                loggedPanel.removeAll(); // Usunięcie przycisków ludzi zalogowanych
                klient.logOut();
            }
        }
    }

    /** Akcja przycisków z listy zalogowanych */
    private class listButs implements ActionListener {
        Logged theGuy;
        listButs(Logged clicked) {
            theGuy = clicked;
        }
        /** Zmienia aktualnego rozmówcę. Wypisuje konwersację ze Stringa 'conversation' itp. */
        public void actionPerformed(ActionEvent ae) {
            theGuy.setEnabled(false);
            theGuy.setText(theGuy.nick);
            if(currentPerson != null)
                currentPerson.setEnabled(true); // Aktywuj przycisk poprzedniego rozmówcy
            currentPerson = theGuy;
            conv.setText(currentPerson.conversation); // Wypisz konwersację
            currentStateLabel.setText("Rozmówca:  " + currentPerson.nick);
            if(!send.isEnabled() && !serverDown)
                send.setEnabled(true); // Aktywuj przycisk 'Wyślij'
        }
    }

    /** Wypisuje nową wiadomość na dole konwersacji lub dopisuje '(n)' obok nicku nadawcy */
    public void writeMess(Message mess) {
        if(currentPerson != null && mess.fromWho.equals(currentPerson.nick)) { // Jeśli nowa wiadomość od aktualnego rozmówcy
            if (currentPerson.conversation != null) { // Jak coś już jest, to po odstępach
                if (!currentPerson.czyJa) // Jeśli ostatnio napisał rozmówca
                    currentPerson.conversation += "\n" + mess.text;
                else
                    currentPerson.conversation += "\n\n@" + mess.fromWho + "\n" + mess.text;
            }
            else
                currentPerson.conversation = "@" + mess.fromWho + "\n" + mess.text;
            conv.setText(currentPerson.conversation);
            currentPerson.setCzyJa(false);
        }
        else { // Jeśli nowa wiadomość od innego rozmówcy to sprawdź od którego
            Iterator iterator = list.iterator();
            while(iterator.hasNext()) {
                Logged guy = (Logged) iterator.next();
                if(mess.fromWho.equals(guy.nick)) { // Jeśli to od tego rozmówcy
                    if (guy.conversation != null) { // Jak coś już jest, to po odstępach
                        if (!guy.czyJa) // Jeśli ostatnio napisał rozmówca
                            guy.conversation += "\n\n" + mess.text;
                        else
                            guy.conversation += "\n\n@" + mess.fromWho + "\n" + mess.text;
                    }
                    else
                        guy.conversation = "@" + mess.fromWho + "\n" + mess.text;
                    guy.setText(guy.nick + " (n)"); // +(n) obok nicku na przycisku
                    guy.setCzyJa(false);
                    break;
                }
            }
        }
    }

    /** Tworzy listę zalogowanych */
    void listCreate(String[] nL) {

        if(nL[0] == null) { // Jeśli pusta lista to nic nie rób
            System.out.println("View: Pusta lista");
            return;
        }
        System.out.println("View: " + Arrays.toString(nL));

        for(numberOfLogged = 0; nL[numberOfLogged] != null; numberOfLogged++) {
            Logged newGuy = new Logged(nL[numberOfLogged]);
            newGuy.addActionListener(new listButs(newGuy));
            list.add(newGuy); // Dodaj do listy zalogowanych
            loggedPanel.add(newGuy); // Dodaj do panelu zalogowanych
        }
        currentStateLabel.setText("Wybierz rozmówcę");
        loggedLabel.setText(" ZALOGOWANI(" + numberOfLogged + ") ");
    }

    /** Aktualizuje listę zalogowanych */
    void listUpdate(InOut listChanger) {
        System.out.println("listUpdate: " + listChanger.nick);
        if(listChanger.in) { // Jeśli nowy zalogowany
            loggedLabel.setText(" ZALOGOWANI(" + ++numberOfLogged + ") ");
            Logged newGuy = new Logged(listChanger.nick);
            newGuy.addActionListener(new listButs(newGuy));
            list.add(newGuy); // Dodaj do listy zalogowanych
            loggedPanel.add(newGuy); // Dodaj do panelu zalogowanych
            if(!send.isEnabled()) { // Jeśli nie wybrano jeszcze rozmówcy -> napisz zachętę
                currentStateLabel.setText(listChanger.nick + " się zalogował!");
            }
            else // Jeśli już wybrano rozmówcę, potrzeba odświeżenia
                refresh();
        }
        else { // "listChanger" się wylogował
            loggedLabel.setText(" ZALOGOWANI(" + --numberOfLogged + ") ");
            Iterator iterator = list.iterator();
            while(iterator.hasNext()) {
                Logged byeGuy = (Logged) iterator.next();
                if(byeGuy.nick.equals(listChanger.nick)) {
                    iterator.remove();
                    loggedPanel.remove(byeGuy);
                    if(currentPerson == byeGuy) {
                        currentPerson = null;
                        send.setEnabled(false);
                        conv.setText("");
                        if(numberOfLogged > 0)
                            currentStateLabel.setText("Wybierz rozmówcę");
                        else
                            currentStateLabel.setText("Brak rozmówcy");
                    }
                    refresh();
                    break;
                }
            }
        }
    }

    void loggingDone() {
        frame.setVisible(true);
        meLabel.setText("Jesteś zalogowany/na jako:   " + klient.getMe());
        currentStateLabel.setText("Brak rozmówcy");
    }

    void serverDown() {
        serverDown = true;
        meLabel.setText("UTRACONO POŁĄCZENIE Z SERVEREM");
        loggedLabel.setText(" Odłączeni(" + numberOfLogged + ") ");
        send.setEnabled(false);
        options.setEnabled(false);
    }

    private void refresh() {
        frame.setVisible(false);
        frame.setVisible(true);
    }
}


//    @Override
//    public void setWindowEnabled(boolean b) {
//        messageContent.setEnabled(b);
//        send.setEnabled(b);
//    }
//
//    @Override
//    public void finish() {
//        frame.setVisible(false);
//        frame.dispose();
//        klient.finish();
//        System.exit(0);
//    }