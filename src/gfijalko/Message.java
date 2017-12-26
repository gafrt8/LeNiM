package gfijalko;

import java.io.*;

/** Obiekty tej klasy są przesyłne między klientem a serwerem */

public class Message implements Serializable {
    String text;
    String fromWho;
    String toWho;
    boolean logout = false;

    public Message(String t, String fW, String tW) {
        text = t;
        fromWho = fW;
        toWho = tW;
    }
}