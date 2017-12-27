package gfijalko;

import java.io.*;

/** Obiekty tej klasy są przesyłne między klientem a serwerem */

public class Message implements Serializable {
    /** Treść wiadomości lub nick (przy logowaniu) */
    String text;
    /** Od kogo (nick) */
    String fromWho;
    /** Do kogo (nick) */
    String toWho;
    /** 0 - zwykła wiadomość, 1 - zaloguj, 2 - wyloguj */
    int logInfo = 0;

    /** Konstruktor zwykłych wiadomości */
    Message(String t, String fW, String tW) {
        text = t;
        fromWho = fW;
        toWho = tW;
    }

    /** Konstruktor wiadomości: zaloguj/wyloguj (od Klienta) */
    Message(String t, int l) {
        text = t; // nick petenta
        logInfo = l;
    }

    /** Konstruktor wiadomości: feedback (od Servera) */
    Message(int l) {
        logInfo = l;
    }
}