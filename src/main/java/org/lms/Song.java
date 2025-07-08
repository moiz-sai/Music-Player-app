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
    // Remove unused static Player variable
    private String songTitle;
    private String songArtist;
    private String duration;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMiliSeconds;
    private Artwork artwork;
    private Tag tag;
    private JLabel albumArtLabel = new JLabel();

    public Song(String filepath) {

        this.filePath = filepath;
        try {
            mp3File = new Mp3File(filepath);
            frameRatePerMiliSeconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            duration = convertToSongLengthFormat();
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            tag = audioFile.getTag();
            if (tag != null) {
                this.songTitle = tag.getFirst(FieldKey.TITLE);
                this.songArtist = tag.getFirst(FieldKey.ARTIST);
            } else {
                this.songTitle = "N/A";
                this.songArtist = "N/A";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public ImageIcon getSongArtwork() {
        try {
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            Tag tag = audioFile.getTag();

            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    ImageIcon icon = new ImageIcon(imageData);

                    // Scale the image to fit your label size
                    Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String convertToSongLengthFormat(){
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

    public void setAlbumArtLabel(JLabel albumArtLabel) {
        this.albumArtLabel = albumArtLabel;
    }
    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

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