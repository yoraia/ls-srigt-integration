package net.livesplitintegration;

import java.io.PrintWriter;
import java.net.Socket;

public class LivesplitController {

    private Socket socket;
    private PrintWriter out;

    private boolean wasZero = true;
    private boolean timerRunning = false;

    public void connect() {
        try {
            socket = new Socket("127.0.0.1", 16834);
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("[Livesplit Integration] Connected to Livesplit Server");

        } catch (Exception e) {
            System.out.println("[Livesplit Integration] Failed to connect to Livesplit Server");
        }
    }

    public void disconnect() {
        try {
            if (out != null) {
                out.close();
                out = null;
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }

            wasZero = true;
            timerRunning = false;

            System.out.println("[Livesplit Integration] Disconnected from Livesplit Server");

        } catch (Exception e) {
            System.out.println("[Livesplit Integration] Failed to disconnect from Livesplit Server"); //how
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void sendCommand(String cmd) {
        if (out != null) {
            out.println(cmd);
        }
    }

    public void startRun() {
        sendCommand("reset");
        sendCommand("starttimer");
        sendCommand("pausegametime");

        LivesplitIntegration.get().getSplitManager().reset();
    }

    public void split() {
        sendCommand("split");
        sendCommand("pausegametime");
    }

    // uses the timer going from 0 to not 0 as a way to reset the run. stupid but it works - i think srigt makes new timer objects each world so maybe could use that instead
    // every 0.5s it sends a command to the livesplit server to update the game time
    public void updateGameTime(long ms) {
        if (ms == 0) {
            wasZero = true;
            timerRunning = false;
            return;
        }

        if (wasZero && !timerRunning) {
            startRun();
            wasZero = false;
            timerRunning = true;
        }

        sendCommand("setgametime " + formatTime(ms));
    }

    //convert ms to hh:mm:ss.ms format
    private String formatTime(long ms) {
        long hours = ms / (1000 * 60 * 60);
        long minutes = (ms / (1000 * 60)) % 60;
        long seconds = (ms / 1000) % 60;
        long milliseconds = ms % 1000;

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
    }

}