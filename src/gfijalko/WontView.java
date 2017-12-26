package gfijalko;

/** Wątek GOP'u */

public class WontView implements Runnable {

    private View view;
    private Klient klient;

    /** Konstruktor - przypisuje referencję do klienta aby przekazać ją do GOP'u */
    public WontView(Klient klient) {
        this.klient = klient;
    }

    /** Tworzy GOP z referencją do klienta-twórcy */
    public void run() {
        view = new View(klient);
    }

    /** Zwraca referencję do GOP'u */
    View getRefToView() {
        return view;
    }
}