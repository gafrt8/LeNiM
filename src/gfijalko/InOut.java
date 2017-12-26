package gfijalko;

import java.io.Serializable;

public class InOut implements Serializable {
    String nick;
    /** Jeśli 'true' -> nowy zalogowany, jeśli 'false' -> wylogował się */
    boolean in;
    InOut(String nick, boolean in) {
        this.nick = nick;
        this.in = in;
    }
}