package gfijalko;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/** Controller (Klient) - obsługuje GOP i komunikuje się z serverem */

public class Klient {

    /** Referencja do GOP */
    private View view;
    /** na razie stały login */
    private String me = "ToYa";
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private LoggingWindow loggingWindow;

    /** Konstruktor - Startuje wątek GOP'u, uzyskuje referencję do GOP'u, nawiązuje połączenie z serwerem */
    public Klient() throws IOException {
        WontView wV;
        Thread wontView = new Thread(wV = new WontView(this)); // Utwórz wątek GOP'u
        wontView.start(); // Startuj wątek GOP'u
//        view = wV.getRefToView(); // Przypisz referencję do GOP'u

        socket = new Socket(LetsGo.HOST, LetsGo.PORT);
//        socket2 = new Socket(LetsGo.HOST, LetsGo.port2);
        oos = new ObjectOutputStream(socket.getOutputStream()); // strumień wyjściowy wiadomości
        ois = new ObjectInputStream(socket.getInputStream()); // strumień wejściowy wiadomości
//        ois2 = new ObjectInputStream(socket2.getInputStream()); // strumień wejściowy listy zalogowanych
        (new Thread(new CheckMess())).start(); // Odpalenie wątku nasłuchu wiadomości
//        (new Thread(new CheckList())).start(); // Odpalenie wątku nasłuchu listy zalogowanych

        loggingWindow = new LoggingWindow(this); // Odpal okno logowania
    }

    /** Wysyła wiadomość do serwera (funkcja wywoływana przez View) */
    void sendMess(Message mess) {
        try {
            oos.writeObject(mess);
            oos.flush();
        } catch (Exception e) {
            System.out.println("Klient DOWN sM: " + e);
        }
        System.out.println("Klient: " + mess.text + " do: " + mess.toWho);
    }

    /** Zwraca login klienta */
    String getMe() {
        return me;
    }

    /** Dostaje referencję do GOP'u */
    void refToView(View ref) {
        view = ref;
    }

    /** Wątek - Sprawdza czy jest nowa wiadomość i jak jest to wypisuje */
    private class CheckMess implements Runnable {
        Message mess;
        String[] nickList;
        public void run() {
            while(true) {
                try {
                    mess = (Message) ois.readObject();
                    if(mess.logInfo == LetsGo.LOG_OK)
                        view.writeMess(mess);
                    else if(mess.logInfo == LetsGo.LOG_ACCEPTED) {
                        me = loggingWindow.getLogin(); // Przypisanie zaakceptowanego nicku

                        try { // Odbiór pełnej aktualnej listy (tylko raz, po zalogowaniu)
                            nickList = (String[]) ois.readObject();
                            System.out.println("Klient: " + Arrays.toString(nickList));
                            view.listCreate(nickList); // info do View
                        } catch (Exception e) {
                            System.out.println("Klient DOWN CM1: " + e);
                        }

                        loggingWindow.loggingDone(); // Zamknij okno logowania
                        view.loggingDone(); // Otwórz główne okno programu
                    }
                    else if(mess.logInfo == LetsGo.LOG_REJECTED) {
                        loggingWindow.tryAgain();
                    }
                    else if(mess.logInfo == LetsGo.LIST_UPDATE) {
                        view.listUpdate(mess.listChanger); // info do View
                    }
                } catch (Exception e) {
                    System.out.println("Klient DOWN CM2: " + e);
                }
            }
        }
    }

//    /** Wątek - Odbiera listę zalogowanych i sprawdza czy zaszła zmiana w liście zalogowanych i aktualizuje */
//    private class CheckList implements Runnable {
//        String[] nickList;
//        InOut listChanger;
//        public void run() {
//            try { // To tylko raz, po zalogowaniu
//                nickList = (String[]) ois2.readObject(); // Odbiór pełnej aktualnej listy
//                System.out.println("Klient: " + Arrays.toString(nickList));
//                view.listCreate(nickList); // info do View
//            } catch (Exception e) {
//                System.out.println("Klient DOWN CL1: " + e);
//            }
//            while(true) {
//                try {
//                    listChanger = (InOut) ois2.readObject();
//                    view.listUpdate(listChanger); // info do View
//                } catch (Exception e) {
//                    System.out.println("Klient DOWN CL2: " + e);
//                }
//            }
//        }
//    }
}