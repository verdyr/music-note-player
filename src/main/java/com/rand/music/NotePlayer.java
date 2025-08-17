package com.rand.music;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

public class NotePlayer {

    private static final Map<String, Double> BASE_FREQUENCIES = new HashMap<>();

    static {
        BASE_FREQUENCIES.put("C", 16.35);
        BASE_FREQUENCIES.put("C#", 17.32);
        BASE_FREQUENCIES.put("D", 18.35);
        BASE_FREQUENCIES.put("D#", 19.45);
        BASE_FREQUENCIES.put("E", 20.60);
        BASE_FREQUENCIES.put("F", 21.83);
        BASE_FREQUENCIES.put("F#", 23.12);
        BASE_FREQUENCIES.put("G", 24.50);
        BASE_FREQUENCIES.put("G#", 25.96);
        BASE_FREQUENCIES.put("A", 27.50);
        BASE_FREQUENCIES.put("A#", 29.14);
        BASE_FREQUENCIES.put("B", 30.87);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar target/note-player-1.0-SNAPSHOT.jar \"song\"");
            System.out.println("Example song:");
            System.out.println("C4 D4 E4 F4 G4 A4 B4 C5 C4,E4,G4:600 D4,F4,A4:500");
            return;
        }

        String song = String.join(" ", args);
        playSong(song);
    }

    private static void playSong(String song) {
        String[] elements = song.split("\\s+");
        for (String element : elements) {
            double duration = 500; // default ms

            // Check for optional duration e.g., C4,E4,G4:600
            if (element.contains(":")) {
                String[] parts = element.split(":");
                element = parts[0];
                try {
                    duration = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid duration, using default 500ms");
                }
            }

            // Split by comma for chords
            String[] notes = element.split(",");
            double[] freqs = new double[notes.length];
            boolean valid = true;
            for (int i = 0; i < notes.length; i++) {
                Double freq = getFrequency(notes[i].toUpperCase());
                if (freq != null) {
                    freqs[i] = freq;
                } else {
                    System.out.println("Unknown note: " + notes[i]);
                    valid = false;
                    break;
                }
            }

            if (valid) {
                if (freqs.length == 1) {
                    System.out.println("Playing note: " + notes[0] + " (" + String.format("%.2f", freqs[0]) + " Hz)");
                } else {
                    System.out.println("Playing chord: " + element);
                }
                playChord(freqs, (int) duration);
            }
        }
    }

    private static Double getFrequency(String note) {
        // Extract note and octave
        String notePart = note.replaceAll("[0-9]", "");
        String octavePart = note.replaceAll("[^0-9]", "");

        if (!BASE_FREQUENCIES.containsKey(notePart)) return null;

        int octave = 4; // default octave
        if (!octavePart.isEmpty()) {
            try {
                octave = Integer.parseInt(octavePart);
            } catch (NumberFormatException ignored) {}
        }

        // Frequency = base * 2^octave
        return BASE_FREQUENCIES.get(notePart) * Math.pow(2, octave);
    }

    private static void playChord(double[] freqs, int durationMs) {
        float sampleRate = 44100;
        byte[] buf = new byte[1];
        AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);

        try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
            sdl.open(af);
            sdl.start();

            for (int i = 0; i < durationMs * (float) sampleRate / 1000; i++) {
                double sample = 0;
                for (double freq : freqs) {
                    sample += Math.sin(i / (sampleRate / freq) * 2.0 * Math.PI);
                }
                // Average the chord
                sample /= freqs.length;
                buf[0] = (byte) (sample * 127);
                sdl.write(buf, 0, 1);
            }
            sdl.drain();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
