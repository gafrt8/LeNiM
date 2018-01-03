package gfijalko;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/** Controller (Klient) - obsługuje GOP i komunikuje się z serverem */

public class Klient {

    /** Referencja do GOP */
    private View view;
    /** Login */
    private String me;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private LoggingWindow loggingWindow;

    public static void main(String[] args) { // Do niezależnego odpalania klientów
        Thread klient = new Thread(new WontKli());
        klient.start();
    }

    /** Konstruktor - Startuje wątek GOP'u, uzyskuje referencję do GOP'u, nawiązuje połączenie z serwerem */
    public Klient() throws IOException {
        (new Thread(new WontView(this))).start(); // Startuj wątek GOP'u

        socket = new Socket(LetsGo.HOST, LetsGo.PORT);
        oos = new ObjectOutputStream(socket.getOutputStream()); // strumień wyjściowy wiadomości
        ois = new ObjectInputStream(socket.getInputStream()); // strumień wejściowy wiadomości
        (new Thread(new CheckMess())).start(); // Odpalenie wątku nasłuchu wiadomości

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

    /** Wątek - Sprawdza czy jest nowa wiadomość i jak jest to odpowiednio reaguje */
    private class CheckMess implements Runnable {
        Message mess;
        String[] nickList;
        public void run() {
            try {
                while(true) {
                    mess = (Message) ois.readObject();
                    if(mess.info == LetsGo.TEXT_MESSAGE) // Zwykła wiadomość od innego użytkownika
                        view.writeMess(mess);
                    else if(mess.info == LetsGo.LOG_ACCEPTED) {
                        me = loggingWindow.getLogin(); // Przypisanie zaakceptowanego nicku

                        try { // Odbiór pełnej aktualnej listy (tylko raz, po zalogowaniu)
                            nickList = (String[]) ois.readObject();
                            System.out.println("Klient: " + Arrays.toString(nickList));
                            view.listCreate(nickList); // info do View
                        } catch (Exception e) {
                            System.out.println("Klient DOWN CM1: " + e);
                        }

                        loggingWindow.setLoggingVisible(false); // Zamknij okno logowania
                        view.loggingDone(); // Otwórz główne okno programu
                    }
                    else if(mess.info == LetsGo.LOG_REJECTED) {
                        loggingWindow.tryAgain();
                    }
                    else if(mess.info == LetsGo.LIST_UPDATE) {
                        view.listUpdate(mess.listChanger); // info do View
                    }
                    else if(mess.info == LetsGo.RIP) { // Można zakończyć
                        try {
                            ois.close(); // Zamknij strumień wejściowy
                            oos.close(); // Zamknij strumień wyjściowy
                            socket.close(); // Zamknij gniazdko
                        } catch(IOException e) {
                            System.out.println("Klient DOWN CM2: " + e);
                        }
                        System.exit(0);
                        return;
                    }
                    else if(mess.info == LetsGo.SERVER_DOWN) {
                        view.serverDown();
                        sendMess(mess);
                        try {
                            ois.close(); // Zamknij strumień wejściowy
                            oos.close(); // Zamknij strumień wyjściowy
                            socket.close(); // Zamknij gniazdko
                        } catch(IOException e) {
                            System.out.println("Klient DOWN CM3: " + e);
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("Klient DOWN CM4: " + e);
            }
        }
    }

    /** Zwraca login klienta */
    String getMe() {
        return me;
    }

    /** Dostaje referencję do GOP'u */
    void refToView(View ref) {
        view = ref;
    }

    /** Wysyła prośbę do servera o wylogowanie. Ponowne logowanie */
    void logOut() {
        sendMess(new Message(LetsGo.LOG_OUT));
        loggingWindow.setLoggingVisible(true); // Otwórz okno logowania
    }

    /** Wysyła informację do servera o zakończeniu pracy */
    void finish() {
        sendMess(new Message(LetsGo.RIP));
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