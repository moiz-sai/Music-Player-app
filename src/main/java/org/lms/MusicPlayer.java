package org.lms;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class MusicPlayer extends PlaybackListener {
    private Song currentSong;
    private ModernGui gui;

    private AdvancedPlayer advancedPlayer;
    private boolean isPaused = false;

    private int currentFrame = 0;
    private int currentTimeInMilliseconds = 0;

    private static final Object playSignal = new Object();

    public MusicPlayer(ModernGui gui) {
        this.gui = gui;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentFrame(int frame) {
        this.currentFrame = frame;
        this.currentTimeInMilliseconds = (int) ((frame )/ currentSong.getFrameRatePerMiliSeconds());
    }

    public void setCurrentTimeinMili(int timeInMilliseconds) {
        this.currentTimeInMilliseconds = timeInMilliseconds;
        this.currentFrame = (int) (timeInMilliseconds * currentSong.getFrameRatePerMiliSeconds());
    }

    public void loadSong(Song song) {
        this.currentSong = song;
        if (currentSong != null) {
            resetPlaybackPosition();
            playCurrentSong();
        }
    }

    public void playCurrentSong() {
        if (currentSong == null) return;

        try {
            FileInputStream fis = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bis = new BufferedInputStream(fis);
            advancedPlayer = new AdvancedPlayer(bis);
            advancedPlayer.setPlayBackListener(this);

            startMusicThread();
            startPlaybackSliderThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseSong() {
        if (advancedPlayer != null) {
            isPaused = true;
            stopSong();
        }
    }

    public void stopSong() {
        if (advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void resetPlaybackPosition() {
        currentFrame = 0;
        currentTimeInMilliseconds = 0;
        isPaused = false;
    }

    private void startMusicThread() {
        new Thread(() -> {
            try {
                if (isPaused) {
                    isPaused = false;
                    synchronized (playSignal) {
                        playSignal.notify();
                    }
                    advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                } else {
                    advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startPlaybackSliderThread() {
        new Thread(() -> {
            while (true) {
                synchronized (playSignal) {
                    while (isPaused) {
                        try {
                            playSignal.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                if (!isPaused) {
                    try {
                        currentTimeInMilliseconds++;
                        int calculatedFrame = (int) (currentTimeInMilliseconds * currentSong.getFrameRatePerMiliSeconds());
                        gui.setPlayerSliderValue(calculatedFrame);
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback started");
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback finished");
        if (isPaused) {
            // Store current position
            currentTimeInMilliseconds += evt.getFrame(); // very rough estimate
            currentFrame = (int) (currentTimeInMilliseconds * currentSong.getFrameRatePerMiliSeconds());
            System.out.println("Paused at frame: " + currentFrame);
        } else {
            resetPlaybackPosition(); // reset if finished naturally
        }
    }
}
