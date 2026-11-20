package bac.com.mosweu.ooad_project;

import javafx.application.Application;

/** Starts the JavaFX banking application. */
public class Launcher {

    /** Delegates JavaFX startup to {@link MainApp}. */
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
