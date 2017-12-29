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
    private JLabel currentStateLabel, meLabel;
    /** Pola tekstowe: messageContent i conversation */
    private JTextArea mCont, conv;
    /** Aktualny rozmówca */
    private Logged currentPerson;
    /** Lista zalogowanych klientów */
    private ArrayList <Logged> list;

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
        konLabel.setHorizontalAlignment(konLabel.CENTER);
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
        mCont = new JTextArea();
        mCont.setMargin(new Insets(10,10,10,10));
        mCont.setLineWrap(true); // Zawijaj tekst
        mCont.setWrapStyleWord(true); // Zawijaj tak, żeby słów nie cięło
        mCont.setBackground(Color.cyan);
        JScrollPane jSPm = new JScrollPane(mCont); // Żeby scrollować się dało
/*Treść wiadomości (CENTER)*/

/*lista klientów (LINE_END)*/
        JPanel jPaLE = new JPanel();
        BorderLayout bLyLE = new BorderLayout();
        jPaLE.setLayout(bLyLE);
        jPaLE.setBackground(Color.lightGray);

        JLabel zalLabel = new JLabel(" ZALOGOWANI: ");
        zalLabel.setHorizontalAlignment(zalLabel.CENTER);
        jPaLE.add(zalLabel, BorderLayout.PAGE_START);

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

        options = new JButton("OPCJE");
        jPaPE.add(options);

        currentStateLabel = new JLabel("Brak rozmówcy");
        currentStateLabel.setHorizontalAlignment(JLabel.CENTER);
        jPaPE.add(currentStateLabel);

        send = new JButton("WYŚLIJ");
        send.setEnabled(false);
        send.addActionListener(new sendBut());
        jPaPE.add(send);
/*dolny kontener (PAGE_END)*/

/*cała ramka*/
        JPanel jPaF = new JPanel();
        BorderLayout bLyF = new BorderLayout(); // Układ GOP'u
        bLyF.setHgap(5);
        bLyF.setVgap(5);
        jPaF.setLayout(bLyF);
        Color color = new Color(255, 230, 204);
        jPaF.setBackground(color);

        jPaF.add(meLabel, BorderLayout.PAGE_START);
        jPaF.add(jPaLS, BorderLayout.LINE_START);
        jPaF.add(jSPm, BorderLayout.CENTER);
        jPaF.add(jPaLE, BorderLayout.LINE_END);
        jPaF.add(jPaPE, BorderLayout.PAGE_END);

        frame = new JFrame("LeNiM");
        frame.setLocation(300, 200);
        frame.setSize(550, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Docelowo: ostrzeżenie!
        frame.add(jPaF);
/*cała ramka*/
    }

    /** Akcja przycisku "SEND" */
    private class sendBut implements ActionListener {
        /** Przekazuje wiadomość klientowi i wypisuje ją na dole konwersacji */
        public void actionPerformed(ActionEvent ae) {

            if(mCont.getText().equals("")) return; // Nie wysyłaj gdy brak tekstu

            Message mess = new Message(mCont.getText(), klient.getMe(), currentPerson.nick);
            klient.sendMess(mess); // Prześlij Klientowi treść wiadomości

            // Dopisz do konwersacji:
            if(currentPerson.conversation != null) { // Jak coś już jest, to po odstępach
                if (currentPerson.czyJa) // Jeśli ostatnio napisałem ja
                    currentPerson.conversation += "\n\n" + mCont.getText();
                else
                    currentPerson.conversation += "\n\n@ja\n" + mCont.getText();
            }
            else
                currentPerson.conversation = "@ja\n" + mCont.getText();

            conv.setText(currentPerson.conversation);
            currentPerson.setCzyJa(true);
            mCont.setText(""); // Czyść messageContent
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
            currentPerson = theGuy;
            conv.setText(currentPerson.conversation); // Wypisz konwersację
            currentStateLabel.setText("Rozmówca:  " + currentPerson.nick);
            if(!send.isEnabled()) // Aktywuj przycisk 'Wyślij' jeśli nie jest aktywny
                send.setEnabled(true);
        }
    }

    /** Wypisuje nową wiadomość na dole konwersacji lub dopisuje '(n)' obok nicku nadawcy */
    public void writeMess(Message mess) {
        if(currentPerson != null && mess.fromWho.equals(currentPerson.nick)) { // Jeśli nowa wiadomość od aktualnego rozmówcy
            if (currentPerson.conversation != null) { // Jak coś już jest, to po odstępach
                if (!currentPerson.czyJa) // Jeśli ostatnio napisał rozmówca
                    currentPerson.conversation += "\n\n" + mess.text;
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

        Logged newGuy;
        System.out.println("View: " + Arrays.toString(nL));

        try {
            TimeUnit.MILLISECONDS.sleep(900); // Opóźnienie w celu eliminacji wyścigów
        } catch(InterruptedException ie) {
            System.out.println(ie);
        }

        for(int i=0; nL[i] != null; i++) {
            newGuy = new Logged(nL[i]);
            list.add(newGuy); // Dodaj do listy zalogowanych
            loggedPanel.add(newGuy); // Dodaj do panelu zalogowanych
            newGuy.addActionListener(new listButs(newGuy));
        }
        currentStateLabel.setText("Wybierz rozmówcę");
    }

    /** Aktualizuje listę zalogowanych */
    void listUpdate(InOut who) {
        if(who.in) { // Jeśli nowy zalogowany
            Logged newGuy = new Logged(who.nick);
            list.add(newGuy); // Dodaj do listy zalogowanych
            loggedPanel.add(newGuy); // Dodaj do panelu zalogowanych
            newGuy.addActionListener(new listButs(newGuy));
            if(!send.isEnabled()) { // Jeśli nie wybrano jeszcze rozmówcy -> napisz zachętę
                currentStateLabel.setText(who.nick + " się zalogował!");
            }
        }
        else { // "who" się wylogował
            Iterator iterator = list.iterator();
            while(iterator.hasNext()) {
                Logged byeGuy = (Logged) iterator.next();
                if(byeGuy.nick.equals(who.nick)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    void loggingDone() {
        frame.setVisible(true);
        meLabel.setText("Jesteś zalogowany/na jako:   " + klient.getMe());
    }
}