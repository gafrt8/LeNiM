package gfijalko;

import java.io.*;
import java.net.*;
import java.util.*;


/** Controller (Server) - przekazuje wiadomości między klientami i obsługuje listę zalogowanych */

public class Server implements Runnable {

    private Socket socket, socket2;
    /** Lista zalogowanych klientów */
    private ArrayList <GetAndPass> list;
    /** Lista nicków zalogowanych klientów */
    private String[] nickList;
    /** Licznik zalogowanych klientów */
    private int numberOfLogged = 0;

    /** Odpala wątek szukania nowych klientów i tworzy listę gap'ów */
    public void run() {
        list = new ArrayList<>(); // Tworzy listę wątków klientów (gap'ów)
        nickList = new String[LetsGo.LIMIT]; // Tworzy listę nicków klientów. Ilość miejsc ograniczona
        Thread listen = new Thread(new KliSearch());
        listen.start();
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
        System.out.println("Server: " + mess.text + " from " + mess.fromWho + " to " + mess.toWho);
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
        System.out.println("Server: " + listChanger.nick + " (zal -> true): " + listChanger.in);
    }

    /** Przekazuje informację o poprawności logowania */
    private void passInfo(Message feedback, ObjectOutputStream oos) {
        try {
            oos.writeObject(feedback); // Prześlij info
            oos.flush();
        } catch (Exception e) {
            System.out.println("Server DOWN pI: " + e);
        }
        System.out.println("Server: " + "(OK -> 3): " + feedback.logInfo);
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
    }

    /** Wątek - Nasłuchuje nowych klientów i tworzy ich wątki */
    public class KliSearch implements Runnable {

        ObjectInputStream ois;
        ObjectOutputStream oos;

        public void run() {
            try {
                ServerSocket ss = new ServerSocket(LetsGo.PORT); // Gniazdko nasłuchu nowych klientów
//                ServerSocket ss2 = new ServerSocket(LetsGo.port2); // Gniazdko nasłuchu dla przekazywania listy
                while(true) { // nieskończone nasłuchiwanie (na razie - trzeba wprowadzić ograniczenie ilości klientów)
                    socket = ss.accept();
                    System.out.println("Akceptuję połączenie z: " + socket.getInetAddress());
//                    socket2 = ss2.accept();
//                    System.out.println("Akceptuję socket2");
                    ois = new ObjectInputStream(socket.getInputStream()); // Strumień wejściowy wiadomości
                    oos = new ObjectOutputStream(socket.getOutputStream()); // Strumień wyjściowy wiadomości
//                    oos2 = new ObjectOutputStream(socket2.getOutputStream()); // Strumień wyjściowy listy zalogowanych

                    (new Thread(new LogThread(ois, oos))).start(); // Start wątku logowania

                    if(numberOfLogged == LetsGo.LIMIT) // Uśpij gdy limit klientów osiągnięty
                        try {
                            wait();
                        } catch(Exception e) {
                            System.out.println(e);
                        }
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
//            this.oos2 = oos2;
            this.nick = nick;
        }
        /** Odbiera wiadomość i wywołuje przekazywacz */
        public void run() {
            System.out.println("GetAndPass created");
            try {
                while(true) { // Pętla nieskończona odbierania wiadomości
                    mess = (Message) ois.readObject(); // Odczyt obiektu wiadomości
                    passMess(mess); // Przekazanie wiadomości do adresata
                }
            } catch (Exception e) {
                System.out.println("Server DOWN GAP: " + e);
            }
        }
    }

    /** Wątek - obsługuje logowanie, tworzy wątek klienta i dodaje go do listy */
    public class LogThread implements Runnable {
        ObjectInputStream ois;
        ObjectOutputStream oos;
        GetAndPass gap;
        String nick;
        Message mess, feedback;
        boolean isGood;

        LogThread(ObjectInputStream ois, ObjectOutputStream oos) {
            this.ois = ois;
            this.oos = oos;
//            this.oos2 = oos2;
        }

        public void run() {
            try { // Logowanie
                while(true) { // Przechwyć nick (login)
                    mess = (Message) ois.readObject(); // Odczyt obiektu wiadomości

                    if(mess.text.length() >= 3 && mess.text.length() <= 12) { // Badanie długości nicku
                        isGood = true; // Wstępne założenie poprawności
                        for(int i=0; i < numberOfLogged; i++) { // Badanie czy nick się nie powtarza
                            if(mess.text.equals(nickList[i])) {
                                isGood = false;
                                break;
                            }
                        }
                    }
//                    else
//                        isGood = false;

                    if(isGood) {
                        feedback = new Message(LetsGo.LOG_ACCEPTED);
                        passInfo(feedback, oos); // Info o akceptacji nicku dla logującego się
                        nick = mess.text; // Przypisanie nicku
                        break;
                    }
                    else {
                        feedback = new Message(LetsGo.LOG_REJECTED);
                        passInfo(feedback, oos); // Info o odrzuceniu nicku dla logującego się
                    }
                }
            } catch (Exception e) {
                System.out.println("Server DOWN logowanie: " + e);
            }

            (new Thread(gap = new GetAndPass(ois, oos, nick))).start(); // utworzenie i start wątku konkretnego klienta
            try { // Lista dla nowozalogowanego
                oos.writeObject(nickList); // Przesyłamy aktualną listę zalogowanch
                oos.flush();
            } catch (Exception e) {
                System.out.println("Server DOWN lista dla nowozalogowanego: " + e);
            }
            InOut newIn = new InOut(nick, true); // Nowy zalogowany
            passChange(newIn); // Przesłanie wszystkim zalogowanym zmiany w liście
            updateList(newIn); // Dodanie nowozalogowanego do listy nicków
            list.add(gap); // Dodanie do listy klientów
        }
    }
}

// Gdzie to dać:
//                ois.close(); // Zamknij strumień wejściowy
//                oos.close(); // Zamknij strumień wyjściowy
//                socket.close(); // Zamknij gniazdko