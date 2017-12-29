package gfijalko;

import java.io.Serializable;

/** Nick klienta do zalogowania / wylogowania */

public class InOut implements Serializable {
    String nick;
    /** Jeśli 'true' -> nowy zalogowany, jeśli 'false' -> wylogował się */
    boolean in;

    /** Nick klienta i informacja czy do zalogowania czy do wylogowania */
    InOut(String nick, boolean in) {
        this.nick = nick;
        this.in = in;
    }
}