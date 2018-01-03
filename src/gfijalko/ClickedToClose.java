package gfijalko;

/** Interfejs obejmujący akcje wykonywane przez okno ostrzegawcze przed zamknięciem */

public interface ClickedToClose {
    /** setEnabled() */
    void setWindowEnabled(boolean b);
    /** Zakończ */
    void finish();
}
