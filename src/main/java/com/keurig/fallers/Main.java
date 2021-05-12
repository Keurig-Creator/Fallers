package com.keurig.fallers;

import javax.security.auth.login.LoginException;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        File file = new File("config.properties");


        System.out.println("Starting FallerZ Bot");
        try {
            new Bot().start();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
