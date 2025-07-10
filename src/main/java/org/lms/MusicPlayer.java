package org.lms;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {
    private Song currentSong;
    private ModernGui gui;
    private ArrayList<Song> playlist;

    private AdvancedPlayer advancedPlayer;
    private boolean isPaused = false;
    private boolean songFinished = false;
    private boolean isPlaylistMode = false;
    private boolean pressedNext, pressedPrevious;
    private int currentFrame = 0;
    private int currentTimeInMilliseconds = 0;
    private int currentPlaylistIndex;
    private Song playingSong;
    private static final Object playSignal = new Object();

    public MusicPlayer(ModernGui gui) {
        this.gui = gui;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentFrame(int frame) {
        this.currentFrame = frame;
        this.currentTimeInMilliseconds = (int) (frame/currentSong.getFrameRatePerMiliSeconds());
    }



    public void playNextSong(){
        if(playlist == null) return;

        pressedNext = true;

        if(!songFinished) {
            stopSong();
        }

        currentPlaylistIndex++;
        if(currentPlaylistIndex >= playlist.size()){
            currentPlaylistIndex = 0;
        }
        currentSong = playlist.get(currentPlaylistIndex);


        currentFrame = 0;
        currentTimeInMilliseconds = 0;
        gui.enablePauseButtonandDisablePlayButton();
        gui.updateSongArtwork(currentSong);
        gui.updatetitles(currentSong);
        gui.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void previousSong(){
        if(playlist == null) return;

        pressedPrevious = true;

        if(!songFinished) {
            stopSong();
        }
        currentPlaylistIndex--;

        if(currentPlaylistIndex < 0){
            currentPlaylistIndex = playlist.size() - 1;
        }
        currentSong = playlist.get(currentPlaylistIndex);

        currentFrame = 0;
        currentTimeInMilliseconds = 0;
        gui.enablePauseButtonandDisablePlayButton();
        gui.updateSongArtwork(currentSong);
        gui.updatetitles(currentSong);
        gui.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void loadPlaylistFromPaths(ArrayList<String> paths) {
        if (!songFinished) {
            stopSong();
        }
        // Reset playback position
        resetPlaybackPosition();

        playlist = new ArrayList<>();

        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                playlist.add(new Song(path));
            }
        }

        if (!playlist.isEmpty()) {
            gui.setPlayerSliderValue(0);
            currentTimeInMilliseconds = 0;
            currentFrame = 0;
            currentSong = playlist.get(0);

            gui.enablePlayButtonandDisablePauseButton();
            gui.updateSongArtwork(currentSong);
            gui.updatetitles(currentSong);
            gui.updatePlaybackSlider(currentSong);

            playCurrentSong();
        }
    }



    public void setCurrentTimeinMili(int timeInMilliseconds) {
        this.currentTimeInMilliseconds = timeInMilliseconds;
        this.currentFrame = (int) (timeInMilliseconds * currentSong.getFrameRatePerMiliSeconds());
    }

    public void loadSong(Song song) {
        isPlaylistMode = false;
        playlist = null;
        currentPlaylistIndex = 0;

        if(!songFinished) {
            stopSong();
        }

        currentSong = song;
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

            playingSong = currentSong;

            startMusicThread();
            startPlaybackSliderThread();
            gui.updatetitles(currentSong);
            gui.updatePlaybackSlider(currentSong);
            gui.enablePauseButtonandDisablePlayButton();
            gui.updateSongArtwork(currentSong);


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


    //resets slider and frames for new song
    public void resetPlaybackPosition() {
        currentFrame = 0;
        currentTimeInMilliseconds = 0;
        isPaused = false;
    }

    //this thread will be playing if try to load another song and current song will play until new is completley loaded then this thread will be stop
    private void startMusicThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(isPaused){
                        synchronized(playSignal){
                            // update flag
                            isPaused = false;

                            // notify the other thread to continue (makes sure that isPaused is updated to false properly)
                            playSignal.notify();
                        }

                        // resume music from last frame
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else{
                        // play music from the beginning
                        advancedPlayer.play();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startPlaybackSliderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isPaused){
                    try{
                        synchronized(playSignal){
                            playSignal.wait();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                while(!isPaused && !songFinished && !pressedNext && !pressedPrevious){
                    try {
                        currentTimeInMilliseconds++;

                        int calculatedFrame = (int) (currentTimeInMilliseconds * currentSong.getFrameRatePerMiliSeconds());
                        currentFrame = calculatedFrame;
                        gui.setPlayerSliderValue(calculatedFrame);
                        Thread.sleep(1);
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
        songFinished = false;
        pressedNext = false;
        pressedPrevious = false;
    }
    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback finished");

        if (isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMiliSeconds());
            System.out.println("Paused at frame: " + currentFrame);
            return;
        }
        if (playingSong != currentSong) {
            System.out.println("Ignoring playback finished for old song");
            return;
        }
        if (pressedNext || pressedPrevious) return;

        songFinished = true;
        resetPlaybackPosition();
        gui.enablePlayButtonandDisablePauseButton();

        ///only move to next song if in playlist mode
        if (isPlaylistMode && playlist != null && !playlist.isEmpty()&&
                currentPlaylistIndex >= 0 && currentPlaylistIndex < playlist.size()) {
            if (currentPlaylistIndex == playlist.size() - 1) {
                System.out.println("Reached last song. Stopping playback.");
                advancedPlayer = null;
            } else{
                playNextSong();
            }
        }
    }

}
