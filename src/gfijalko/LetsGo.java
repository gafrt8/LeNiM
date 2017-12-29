package gfijalko;

/** Klasa rozpoczynająca działanie programu i definiująca stałe */

public class LetsGo {

    public static String HOST = "localhost";
    public static int PORT = 4444;
    public static int port2 = 5555;
    /** Ograniczenie ilości klientów */
    public static int LIMIT = 10;
    /** Zwykła wiadomość */
    public static int LOG_OK = 0;
    /** Zaloguj */
    public static int LOG_IN = 1;
    /** Wyloguj */
    public static int LOG_OUT = 2;
    /** Zaakceptowano nick */
    public static int LOG_ACCEPTED = 3;
    /** Odrzucono nick */
    public static int LOG_REJECTED = 4;
    /** Aktualizacja listy zalogowanych */
    public static int LIST_UPDATE = 5;

    public static void main(String[] args){
        Thread server = new Thread(new Server());
        server.start();
        Thread klient = new Thread(new WontKli());
        klient.start();
        Thread klient2 = new Thread(new WontKli());
        klient2.start();
    }
}