package com.rand.music;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

public class NotePlayer {

    private static final Map<String, Double> NOTE_FREQUENCIES = new HashMap<>();

    static {
        NOTE_FREQUENCIES.put("C", 261.63);  // Middle C
        NOTE_FREQUENCIES.put("D", 293.66);
        NOTE_FREQUENCIES.put("E", 329.63);
        NOTE_FREQUENCIES.put("F", 349.23);
        NOTE_FREQUENCIES.put("G", 392.00);
        NOTE_FREQUENCIES.put("A", 440.00);  // Concert A
        NOTE_FREQUENCIES.put("B", 493.88);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar target/note-player-1.0-SNAPSHOT.jar <note1> <note2> ...");
            System.out.println("Example: java -jar target/note-player-1.0-SNAPSHOT.jar C D E F G A B");
            return;
        }

        for (String note : args) {
            Double freq = NOTE_FREQUENCIES.get(note.toUpperCase());
            if (freq != null) {
                System.out.println("Playing: " + note + " (" + freq + " Hz)");
                playTone(freq, 500);  // 500 ms per note
            } else {
                System.out.println("Unknown note: " + note);
            }
        }
    }

    private static void playTone(double freq, int durationMs) {
        float sampleRate = 44100;
        byte[] buf = new byte[1];
        AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
        try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
            sdl.open(af);
            sdl.start();
            for (int i = 0; i < durationMs * (float) sampleRate / 1000; i++) {
                double angle = i / (sampleRate / freq) * 2.0 * Math.PI;
                buf[0] = (byte) (Math.sin(angle) * 127);
                sdl.write(buf, 0, 1);
            }
            sdl.drain();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}

