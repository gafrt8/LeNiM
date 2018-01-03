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
    /** Do aktualizacji listy */
    InOut listChanger;
    /** Informacja jaki cel wiadomości */
    int info = LetsGo.TEXT_MESSAGE; // Wartość domyślna - zwykła wiadomość między użytkownikami

    /** Konstruktor zwykłych wiadomości */
    Message(String t, String fW, String tW) {
        text = t;
        fromWho = fW;
        toWho = tW;
    }

    /** Konstruktor wiadomości logującej z nickiem (od Klienta) */
    Message(String t) {
        text = t;
    }

    /** Konstruktor wiadomości: feedback (od Servera) / prośba o wylogowanie od Klienta */
    Message(int lI) {
        info = lI;
    }

    /** Konstruktor wiadomości: zmiana na liście zalogowanych */
    Message(InOut lC, int lI) {
        listChanger = lC;
        info = lI;
    }
}