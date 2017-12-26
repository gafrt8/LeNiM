package gfijalko;

import java.io.*;

/** Obiekty tej klasy są przesyłne między klientem a serwerem */

public class Message implements Serializable {
    String text;
    String fromWho;
    String toWho;
    /** 0 - zwykła wiadomość, 1 - zaloguj, 2 - wyloguj */
    int logInfo = 0;

    /** Konstruktor zwykłych wiadomości */
    Message(String t, String fW, String tW) {
        text = t;
        fromWho = fW;
        toWho = tW;
    }

    /** Konstruktor wiadomości: zaloguj/wyloguj */
    Message(String t, int l) {
        text = t; // nick petenta
        logInfo = l;
    }
}