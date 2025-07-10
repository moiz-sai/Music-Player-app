package org.lms;

import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class Song {
    private String songTitle;
    private String songArtist;
    private String duration;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMiliSeconds;
    private Artwork artwork;
    private Tag tag;
    private JLabel albumArtLabel = new JLabel();
    
    // Cache the artwork to avoid repeated file reads
    private ImageIcon cachedArtwork;
    private boolean artworkLoaded = false;

    public Song(String filepath) {
        this.filePath = filepath;
        
        // Get filename without extension for fallback
        String filename = new File(filepath).getName();
        String filenameWithoutExt = filename.lastIndexOf('.') > 0 ? 
            filename.substring(0, filename.lastIndexOf('.')) : filename;
        
        try {
            mp3File = new Mp3File(filepath);
            frameRatePerMiliSeconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            duration = convertToSongLengthFormat();
            
            // Only read metadata once
            try {
                AudioFile audioFile = AudioFileIO.read(new File(filePath));
                tag = audioFile.getTag();
                
                if (tag != null) {
                    String title = tag.getFirst(FieldKey.TITLE);
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    
                    // Use metadata if available, otherwise use filename
                    this.songTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : filenameWithoutExt;
                    this.songArtist = (artist != null && !artist.trim().isEmpty()) ? artist.trim() : "Unknown Artist";
                } else {
                    // No metadata found, use filename
                    this.songTitle = filenameWithoutExt;
                    this.songArtist = "Unknown Artist";
                }
            } catch (Exception metadataException) {
                // If metadata reading fails, use filename
                this.songTitle = filenameWithoutExt;
                this.songArtist = "Unknown Artist";
                System.out.println("Could not read metadata for: " + filepath + " - using filename");
            }
            
        } catch (Exception e) {
            // If everything fails, still provide basic info
            this.songTitle = filenameWithoutExt;
            this.songArtist = "Unknown Artist";
            this.duration = "00:00";
            e.printStackTrace();
        }
    }
    
    public ImageIcon getSongArtwork() {
        // Return cached artwork if already loaded
        if (artworkLoaded) {
            return cachedArtwork;
        }
        
        // Load artwork once and cache it
        artworkLoaded = true;
        
        try {
            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    ImageIcon icon = new ImageIcon(imageData);
                    
                    // Scale the image to fit your label size
                    Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    cachedArtwork = new ImageIcon(img);
                    return cachedArtwork;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load artwork for: " + filePath);
        }
        
        // No artwork found
        cachedArtwork = null;
        return null;
    }
    
    private String convertToSongLengthFormat(){
        if (mp3File == null) return "00:00";
        
        long minutes = mp3File.getLengthInSeconds() / 60;
        long seconds = mp3File.getLengthInSeconds() % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        return formattedTime;
    }
    
    //getters
    public JLabel getAlbumArtLabel() {
        return albumArtLabel;
    }
    
    public BufferedImage getAlbumArtBufferedImage() {
        if (albumArtLabel != null && albumArtLabel.getIcon() instanceof ImageIcon) {
            return toBufferedImage(((ImageIcon) albumArtLabel.getIcon()).getImage());
        }
        return null;
    }

    // Convert Image to BufferedImage
    private BufferedImage toBufferedImage(Image img) {
        BufferedImage bimage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    //setter
    public void setAlbumArtLabel(JLabel albumArtLabel) {
        this.albumArtLabel = albumArtLabel;
    }
    
    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }
    //getters
    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getDuration() {
        return duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public Mp3File getMp3File() { 
        return mp3File; 
    }

    public double getFrameRatePerMiliSeconds() {
        return frameRatePerMiliSeconds;
    }
}