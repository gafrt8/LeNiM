package gfijalko;

/** Klasa rozpoczynająca działanie programu */

public class LetsGo {

    public static String host = "localhost";
    public static int port = 4444;
    public static int port2 = 5555;
    public static int limit = 10; // ograniczenie ilości klientów

    public static void main(String[] args){
        Thread server = new Thread(new Server());
        server.start();
        Thread klient = new Thread(new WontKli());
        klient.start();
//        Thread klient2 = new Thread(new WontKli());
//        klient2.start();
    }
}