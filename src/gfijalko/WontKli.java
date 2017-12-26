package gfijalko;

import java.io.IOException;

/** Wątek klienta */

public class WontKli implements Runnable {

    /** Konstruktor -  na razie nic nie robi xD */
    public WontKli() {
        ;
    }

    /** Tworzy klienta */
    public void run() {
        try {
            new Klient();
        } catch (IOException ie) {
            System.out.println("WontKli DOWN: " + ie);
        }
    }
}