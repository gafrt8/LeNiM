package gfijalko;

/** Klasa rozpoczynająca działanie programu i definiująca stałe */

public class LetsGo {

    public static String HOST = "localhost";
    public static int PORT = 4444;
    /** Ograniczenie ilości klientów */
    public static int LIMIT = 10;
    /** Zwykła wiadomość między użytkownikami */
    public static int TEXT_MESSAGE = 0;
    /** Prośba Klienta o wylogowanie */
    public static int LOG_OUT = 1;
    /** Zaakceptowano nick */
    public static int LOG_ACCEPTED = 2;
    /** Odrzucono nick */
    public static int LOG_REJECTED = 3;
    /** Aktualizacja listy zalogowanych */
    public static int LIST_UPDATE = 4;
    /** Klient: zakończono, Server: potwierdzam zakończenie */
    public static int RIP = 5;
    /** Server: zakończono, Klienci: potwierdzam zakończenie */
    public static int SERVER_DOWN = 6;

    public static void main(String[] args) {
        Thread server = new Thread(new Server());
        server.start();
    }
}