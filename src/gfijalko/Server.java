package gfijalko;

import java.io.*;
import java.net.*;
import java.util.*;


/** Controller (Server) - przekazuje wiadomości między klientami i obsługuje listę zalogowanych */

public class Server implements Runnable {

    private Socket socket;
    private ServerSocket serverSocket;
    /** Wykonawca wątku szukania nowych klientów */
//    private ExecutorService exec;
    private Thread listenToKlients;
    /** Lista zalogowanych klientów */
    private ArrayList <GetAndPass> list;
    /** Lista nicków zalogowanych klientów */
    private String[] nickList;
    /** Licznik zalogowanych klientów */
    private int numberOfLogged = 0;
    /** Okno servera */
    private ServerWindow serverWindow;

    /** Odpala wątek szukania nowych klientów i tworzy listę gap'ów */
    public void run() {
        list = new ArrayList<>(); // Tworzy listę wątków klientów (gap'ów)
        nickList = new String[LetsGo.LIMIT]; // Tworzy listę nicków klientów. Ilość miejsc ograniczona
//        exec = Executors.newSingleThreadExecutor();
//        exec.execute(new KliSearch());
        listenToKlients = new Thread(new KliSearch());
        listenToKlients.start();
        serverWindow = new ServerWindow(this); // Otwarcie okna servera
    }

    /** Przeszukuje listę zalogowanych i przekazuje wiadomość do adresata */
    private void passMess(Message mess) {
        Iterator iterator = list.iterator();
        GetAndPass gap;
        ObjectOutputStream oos;

        while(iterator.hasNext()) { // Znajdź adresata na liście zalogowanych
            gap = (GetAndPass) iterator.next();
            if(gap.nick.equals(mess.toWho)) { // Jeśli znaleziony
                oos = gap.oos; // Przypisz bufor do przesyłania wiadomości
                try {
                    oos.writeObject(mess); // Prześlij wiadomość
                    oos.flush();
                } catch (Exception e) {
                    System.out.println("Server DOWN pC: " + e);
                }
            }
        }
        serverWindow.setStatements("Server: " + mess.text + " from " + mess.fromWho + " to " + mess.toWho);
    }

    /** Przekazuje wszystkim zalogowanym nowo zalogowanego/wylogowanego */
    private void passChange(InOut listChanger) {
        Iterator iterator = list.iterator();
        GetAndPass gap;
        ObjectOutputStream oos;

        while(iterator.hasNext()) { // Do każdego zalogowanego
            gap = (GetAndPass) iterator.next();
            oos = gap.oos; // Przypisz strumień
            try {
                oos.writeObject(new Message(listChanger, LetsGo.LIST_UPDATE)); // Info kto się zalogował / wylogował
                oos.flush();
            } catch (Exception e) {
                System.out.println("Server DOWN pC: " + e);
            }
        }
        serverWindow.setStatements("Server: " + listChanger.nick + " (zal -> true): " + listChanger.in);
    }

    /** Przekazuje informację o poprawności logowania */
    private void passInfo(Message feedback, ObjectOutputStream oos) {
        try {
            oos.writeObject(feedback); // Prześlij info
            oos.flush();
        } catch (Exception e) {
            System.out.println("Server DOWN pI: " + e);
        }
        serverWindow.setStatements("Server: " + "(OK -> 3): " + feedback.info);
    }

    /** Tworzy / aktualizuje tablicę nick'ów zalogowanych klientów */
    private void updateList(InOut listChanger) {
        if(listChanger.in)
            nickList[numberOfLogged++] = listChanger.nick; // Dodanie nowego nicku na końcu listy
        else {
            Iterator iterator = list.iterator();
            nickList = new String[LetsGo.LIMIT]; // Nowa lista gdy ktoś się wylogował
            GetAndPass gap;

            for(int i=0; iterator.hasNext(); i++) {
                gap = (GetAndPass) iterator.next();
                nickList[i] = gap.nick;
            }
            numberOfLogged--;
        }
        serverWindow.Update(nickList);
    }

    /** Wątek - Nasłuchuje nowych klientów i tworzy ich wątki */
    public class KliSearch implements Runnable {

        ObjectInputStream ois;
        ObjectOutputStream oos;

        public void run() {
            try {
                serverSocket = new ServerSocket(LetsGo.PORT); // Gniazdko nasłuchu nowych klientów
                while(true) { // nieskończone nasłuchiwanie nowych klientów (aż osiągnięty limit)
                    socket = serverSocket.accept();
                    System.out.println("Akceptuję połączenie z: " + socket.getInetAddress());
//                    serverWindow.setStatements("Akceptuję połączenie z: " + socket.getInetAddress());
                    ois = new ObjectInputStream(socket.getInputStream()); // Strumień wejściowy wiadomości
                    oos = new ObjectOutputStream(socket.getOutputStream()); // Strumień wyjściowy wiadomości

//                    (new Thread(new LogThread(ois, oos))).start(); // Start wątku logowania
                    GetAndPass gap = new GetAndPass(ois, oos);
                    new Thread(new LogThread(new Thread(gap), gap));

                    limitChecker(); // Sprawdzenie czy limit klientów osiągnięty
                    System.out.println("Doszło aż tu");
                }
            } catch (IOException ie) {
                System.out.println("Server DOWN KS: " + ie);
            }
        }
    }

    /** Wątek - Odbiera i przesyła wiadomości
     *  oraz udostępnia strumienie by otrzymać wiadomość lub listę zalogowanych
     *  -> obsługa jednego klienta */
    public class GetAndPass implements Runnable {

        ObjectInputStream ois;
        ObjectOutputStream oos;
        String nick;
        Message mess;

        /** Przypisuje strumienie do kontaktu z konkretnym klientem */
        GetAndPass(ObjectInputStream ois, ObjectOutputStream oos, String nick) {
            this.ois = ois;
            this.oos = oos;
            this.nick = nick;
        }
        GetAndPass(ObjectInputStream ois, ObjectOutputStream oos) {
            this.ois = ois;
            this.oos = oos;
        }
        /** Odbiera wiadomość i wywołuje przekazywacz */
        public void run() {
            System.out.println("GetAndPass created");
            try {
                while(true) { // Pętla nieskończona odbierania wiadomości

                    mess = (Message) ois.readObject(); // Odczyt obiektu wiadomości

                    if(mess.info == LetsGo.TEXT_MESSAGE)
                        passMess(mess); // Przekazanie wiadomości do adresata

                    else if(mess.info == LetsGo.LOG_OUT) {
                        InOut outGuy = new InOut(nick, false);

                        Iterator iterator = list.iterator();
                        GetAndPass gap;
                        while(iterator.hasNext()) { // Znajdź outGuy'a na liście zalogowanych
                            gap = (GetAndPass) iterator.next();
                            if(nick.equals(gap.nick)) // Jeśli znaleziony
                                iterator.remove(); // Usunięcie z listy zalogowanych
                        }
                        (new Thread(new LogThread(this))).start(); // Start wątku logowania
                        passChange(outGuy); // Przesłanie informacji o wylogowaniu do zalogowanych
                        updateList(outGuy); // Usunięcie z listy nick'ów
                        return;
                    }
                    else if (mess.info == LetsGo.RIP) {
                        InOut ripGuy = new InOut(nick, false);

                        Iterator iterator = list.iterator();
                        GetAndPass gap;
                        while(iterator.hasNext()) { // Znajdź outGuy'a na liście zalogowanych
                            gap = (GetAndPass) iterator.next();
                            if(gap.nick.equals(nick)) { // Jeśli znaleziony
                                iterator.remove(); // Usunięcie z listy zalogowanych
                                try {
                                    oos.writeObject(mess); // Odeślij wiadomość (potwierdzenie)
                                    oos.flush();
                                    ois.close(); // Zamknij strumień wejściowy
                                    oos.close(); // Zamknij strumień wyjściowy
                                } catch (Exception e) {
                                    System.out.println("Server DOWN pC: " + e);
                                }
                            }
                        }
                        passChange(ripGuy); // Przesłanie informacji o wylogowaniu do zalogowanych
                        updateList(ripGuy); // Usunięcie z listy nick'ów
                        return;
                    }
                    else if(mess.info == LetsGo.SERVER_DOWN) {
                        ois.close();
                        oos.close();
                        numberOfLogged--;
                        finishOK();
                        return;
                    }

                }
            } catch (Exception e) {
                System.out.println("Server DOWN GAP " + nick + ": " + e);
            }
        }

        void setNick(String nick) {
            this.nick = nick;
        }
    }

    /** Wątek - obsługuje logowanie, tworzy wątek klienta i dodaje go do listy */
    public class LogThread implements Runnable {
        Thread thread;
        GetAndPass gap;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        String nick;
        Message mess, feedback;
        boolean isGood; // Domyśle: false

        LogThread(ObjectInputStream ois, ObjectOutputStream oos) {
            this.ois = ois;
            this.oos = oos;
        }

        LogThread(Thread thread, GetAndPass gap) {
            this.thread = thread;
            this.gap = gap;
        }

        LogThread(GetAndPass gap) {
            this.gap = gap;
        }

        public void run() {
            try { // Logowanie
                while(true) { // Przechwyć nick (login)
                    mess = (Message) gap.ois.readObject(); // Odczyt obiektu wiadomości

                    if(mess.text.length() >= 3 && mess.text.length() <= 12) { // Badanie długości nicku
                        isGood = true; // Wstępne założenie poprawności
                        for(int i=0; i < numberOfLogged; i++) { // Badanie czy nick się nie powtarza
                            if(mess.text.equals(nickList[i])) {
                                isGood = false;
                                break;
                            }
                        }
                    }

                    if(isGood) {
                        feedback = new Message(LetsGo.LOG_ACCEPTED);
                        passInfo(feedback, gap.oos); // Info o akceptacji nicku dla logującego się
                        nick = mess.text; // Przypisanie nicku
                        break;
                    }
                    else {
                        feedback = new Message(LetsGo.LOG_REJECTED);
                        passInfo(feedback, gap.oos); // Info o odrzuceniu nicku dla logującego się
                    }
                }
            } catch (Exception e) {
                System.out.println("Server DOWN logowanie: " + e);
            }

//            (new Thread(gap = new GetAndPass(ois, oos, nick))).start(); // utworzenie i start wątku konkretnego klienta
            gap.setNick(nick);
            thread.start();
            try { // Lista dla nowozalogowanego
                gap.oos.writeObject(nickList); // Przesyłamy aktualną listę zalogowanch
                gap.oos.flush();
            } catch (Exception e) {
                System.out.println("Server DOWN lista dla nowozalogowanego: " + e);
            }
            InOut newIn = new InOut(nick, true); // Nowy zalogowany
            passChange(newIn); // Przesłanie wszystkim zalogowanym zmiany w liście
            updateList(newIn); // Dodanie nowozalogowanego do listy nicków
            list.add(gap); // Dodanie do listy klientów
        }
    }

    /** Kończy pracę servera */
    synchronized void finish() {
//        exec.shutdownNow(); // Przestań wyszukiwać nowych klientów
        listenToKlients.stop(); // Przestań wyszukiwać nowych klientów
        Iterator iterator = list.iterator();
        GetAndPass gap;
        Message mess = new Message(LetsGo.SERVER_DOWN);
        while(iterator.hasNext()) { // Do każdego zalogowanego
            gap = (GetAndPass) iterator.next();
            try {
                gap.oos.writeObject(mess); // Info że server padł
                gap.oos.flush();
            } catch (Exception e) {
                System.out.println("Server DOWN finish1: " + e);
            }
        }
        try {
            wait();
            System.out.println("Zamykam sockety");
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            System.out.println("Server DOWN finish2: " + e);
        }
        System.exit(0);
    }

    private synchronized void finishOK() {
        System.out.println("finishOK used");
        if(numberOfLogged == 0)
            notify();
    }

    private synchronized void limitChecker() {
        if(numberOfLogged == LetsGo.LIMIT-1) {
            System.out.println("Limit klientów osiągnięty");
            try {
                wait();
            } catch (InterruptedException ie) {
                System.out.println(ie);
            }
        }
    }

    private synchronized void lessThanLimit() {
        if(numberOfLogged == LetsGo.LIMIT-2)
            notify();
    }
}

// Gdzie to dać:
//                ois.close(); // Zamknij strumień wejściowy
//                oos.close(); // Zamknij strumień wyjściowy
//                socket.close(); // Zamknij gniazdko