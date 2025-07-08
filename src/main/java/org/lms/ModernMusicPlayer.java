package org.lms;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSliderUI;

class ModernGui{

    private static final Color FRAME_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private Song song;
    private MusicPlayer musicPlayer;
    private JFileChooser fileChooser;
    private ImageIcon defaultLogo;
    private ImageIcon defaultBackground;
    private JFrame frame;
    private JMenuItem loadSongs;
    BackgroundPanel backgroundPanel;
    BackgroundPanel contentPanel;
    private JLabel songTitle;
    private JLabel songImage;
    private JLabel songArtist;
    JPanel buttonPanel;
    JSlider playerSlider;
    JButton playButton;
    JButton pauseButton;
    private JSlider volumeSlider;
    public ModernGui() {
        // initializeLookAndFeel();
        initializeUI();
    }
    // Call this method in your constructor or initializeUI()
    private void preloadDefaultImages() {
        try {
            defaultLogo = loadImage("E:/musicapp/modern-music-player/src/main/resources/com/myapp/icons/logo.png");
            defaultBackground = loadImage("E:/musicapp/modern-music-player/src/main/resources/com/myapp/icons/backgroundimage.jpg");

            if (defaultLogo == null) {
                System.err.println("Warning: Could not load default logo");
            }
            if (defaultBackground == null) {
                System.err.println("Warning: Could not load default background");
            }
        } catch (Exception e) {
            System.err.println("Error preloading default images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize dark theme");
        }
    }

    private void initializeUI() {
        frame = new JFrame("Music Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.getContentPane().setBackground(Color.WHITE);
        frame.setSize(600, 600);
        backgroundPanel = new BackgroundPanel("E:/musicapp/modern-music-player/src/main/resources/com/myapp/icons/backgroundimage.png",0.4f);
        backgroundPanel.setLayout(new BorderLayout());
        frame.setContentPane(backgroundPanel);


        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP3 Files", "mp3"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.music", System.getProperty("user.home", ".") + "/Music")));
        fileChooser.setAcceptAllFileFilterUsed(false);

        setupToolBar(fileChooser);
        setupCenterPanel();
        setupControlButtons();

        frame.setResizable(false);
        frame.setVisible(true);
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        private float alpha = 1.0f; // 1.0f = fully opaque, 0.0f = fully transparent

        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public BackgroundPanel(String imagePath, float alpha) {
            this(imagePath);
            this.alpha = alpha;
        }

        // ADD THESE NEW METHODS:
        public void setBackgroundImage(String imagePath) {
            try {
                backgroundImage = ImageIO.read(new File(imagePath));
                repaint();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setBackgroundImage(ImageIcon imageIcon) {
            if (imageIcon != null) {
                this.backgroundImage = imageIcon.getImage();
                repaint();
            }
        }

        public void setBackgroundImage(Image image) {
            this.backgroundImage = image;
            repaint();
        }

        public void setAlpha(float alpha) {
            this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                g2d.dispose();
            }
        }
    }


    private void setupToolBar(JFileChooser fileChooser) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JMenuBar menuBar = new JMenuBar();


        JMenu songMenu = new JMenu("Song");
        loadSongs = new JMenuItem("Load Songs");
        loadSongs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(frame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    song = new Song(selectedFile.getAbsolutePath());

                    // Update UI
                    updateSongArtwork(); // i'm calling this when song is selected method
                    songTitle.setText(song.getSongTitle() != null && !song.getSongTitle().isEmpty() ? song.getSongTitle() : selectedFile.getName());
                    songArtist.setText(song.getSongArtist() != null ? song.getSongArtist() : "Unknown Artist");

                    //stop music
                    if (musicPlayer != null) {
                        musicPlayer.stopSong();
                    }
                    // Play music
                    if (musicPlayer == null) {
                        musicPlayer = new MusicPlayer(ModernGui.this);
                    }
                    musicPlayer.loadSong(song);
                    updatePlaybackSlider(song);
                    playerSlider.setValue(0);
                    enablePauseButtonandDisablePlayButton();

                }
            }
        });
        songMenu.add(loadSongs);
        menuBar.add(songMenu);

        JMenu playlistMenu = new JMenu("Playlist");
        playlistMenu.add(new JMenuItem("Create Playlist"));
        playlistMenu.add(new JMenuItem("Load Playlist"));
        menuBar.add(playlistMenu);

        toolBar.add(menuBar);
        frame.add(toolBar, BorderLayout.NORTH);
    }
    private void enablePauseButtonandDisablePlayButton(){
        if (playButton != null && pauseButton != null) {
            playButton.setVisible(false);
            playButton.setEnabled(false);

            pauseButton.setVisible(true);
            pauseButton.setEnabled(true);
        }
    }

    private void enablePlayButtonandDisablePauseButton(){
        if (playButton != null && pauseButton != null) {
            playButton.setVisible(true);
            playButton.setEnabled(true);

            pauseButton.setVisible(false);
            pauseButton.setEnabled(false);
        }
    }


    public void setPlayerSliderValue(int frame){
        playerSlider.setValue(frame);
    }
    private void setupCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false); // transparency

        // Simple content panel — NOT BackgroundPanel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // transparency

        // Song image
        songImage = new JLabel(loadImage("E:/musicapp/modern-music-player/src/main/resources/com/myapp/icons/logo.png"));
        songImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Song title
        songTitle = new JLabel("No Song Playing");
        songTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Song artist
        songArtist = new JLabel("Artist");
        songArtist.setFont(new Font("Arial", Font.PLAIN, 20));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setAlignmentX(Component.CENTER_ALIGNMENT);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);

        // Slider
        playerSlider = new JSlider(0, 100, 0);
        playerSlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playerSlider.setUI(new BasicSliderUI(playerSlider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle trackBounds = trackRect;
                g2.setColor(new Color(200, 200, 200, 120));
                g2.fillRoundRect(trackBounds.x, trackBounds.y + trackBounds.height / 2 - 2,
                        trackBounds.width, 4, 4, 4);
                int fillWidth = (int) ((double) (slider.getValue() - slider.getMinimum()) /
                        (slider.getMaximum() - slider.getMinimum()) * trackBounds.width);
                g2.setColor(new Color(132, 204, 91));
                g2.fillRoundRect(trackBounds.x, trackBounds.y + trackBounds.height / 2 - 2,
                        fillWidth, 4, 4, 4);
                g2.dispose();
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
                g2.dispose();
            }
        });
        playerSlider.setOpaque(false);
        playerSlider.setFocusable(false);
        playerSlider.setPreferredSize(new Dimension(400, 60));
        playerSlider.setMaximumSize(new Dimension(400, 60));
        playerSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerSlider.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (musicPlayer != null) musicPlayer.pauseSong();
            }
            public void mouseReleased(MouseEvent e) {
                if (musicPlayer != null) {
                    int frame = ((JSlider) e.getSource()).getValue();
                    musicPlayer.setCurrentFrame(frame);
                    musicPlayer.playCurrentSong();
                    playerSlider.setValue(frame);
                    enablePauseButtonandDisablePlayButton();
                }
            }
        });

        // Add to contentPanel
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(songImage);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(songTitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(songArtist);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(playerSlider);
        contentPanel.add(Box.createVerticalGlue());

        centerPanel.add(contentPanel, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);
    }

    private void updatePlaybackSlider(Song song){
        playerSlider.setMaximum(song.getMp3File().getFrameCount());
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        JLabel labelEnding = new JLabel(song.getDuration());
        labelEnding.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnding.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnding);

        playerSlider.setLabelTable(labelTable);
        playerSlider.setPaintLabels(true);
        UIManager.put("Slider.paintValue", false);  // Don't paint value text
        UIManager.put("Slider.thumbWidth", 16);
        UIManager.put("Slider.trackHeight", 4);
        UIManager.put("Slider.trackBorderColor", new Color(200, 200, 200, 80)); // Soft track border
        UIManager.put("Slider.trackFillColor", new Color(100, 200, 255)); // Customize if needed
    }
    // Method to update the image when a song is selected
    public void updateSongArtwork() {
        try {
            if (song != null) {
                ImageIcon artwork = song.getSongArtwork();
                if (artwork != null) {
                    // Update the small song image
                    songImage.setIcon(artwork);
                    backgroundPanel.setBackgroundImage(artwork);
                } else {
                    // Song has no artwork - use preloaded defaults
                    songImage.setIcon(defaultLogo);
                    backgroundPanel.setBackgroundImage(defaultBackground);
                }
            } else {
                // No song loaded - use preloaded defaults
                songImage.setIcon(defaultLogo);
                backgroundPanel.setBackgroundImage(defaultBackground);
             }

            // Force repaint
            songImage.repaint();
            backgroundPanel.repaint();


        } catch (Exception e) {
            System.err.println("Error updating song artwork: " + e.getMessage());
            e.printStackTrace();

            // Fallback to safe state
            songImage.setIcon(defaultLogo);
            backgroundPanel.setBackgroundImage(defaultBackground);
            contentPanel.setBackgroundImage(defaultBackground);
        }
    }

    private void setupControlButtons() {
        buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setPreferredSize(new Dimension(150, 70));
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(new Color(0, 0, 0, 0));

        JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,10));
        buttonPanel2.setOpaque(false);

        JButton prevButton = new JButton(new FlatSVGIcon("com/myapp/icons/step-backward.svg", 32,32));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.setContentAreaFilled(false);
        prevButton.setFocusPainted(false);
        prevButton.setOpaque(false);
        prevButton.setBackground(new Color(0, 0, 0, 0));
        prevButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel2.add(prevButton);

        playButton = new JButton(new FlatSVGIcon("com/myapp/icons/play.svg", 32,32));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.setFocusable(false);
        prevButton.setOpaque(false);
        prevButton.setBackground(new Color(0, 0, 0, 0));
        playButton.setContentAreaFilled(false);
        playButton.setOpaque(false);
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (musicPlayer == null || song == null) {
                    JOptionPane.showMessageDialog(frame,
                            "Please load a song first!",
                            "No Song Loaded",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                enablePauseButtonandDisablePlayButton();
                musicPlayer.playCurrentSong();
            }
        });

        buttonPanel2.add(playButton);

        pauseButton = new JButton(new FlatSVGIcon("com/myapp/icons/pause.svg", 32,32));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setOpaque(false);
        pauseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pauseButton.setBackground(new Color(0, 0, 0, 0));
        pauseButton.setFocusable(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.setOpaque(false);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonandDisablePauseButton();
                musicPlayer.pauseSong();
            }
        });
        buttonPanel2.add(pauseButton);


        JButton forwardButton = new JButton(new FlatSVGIcon("com/myapp/icons/skip-forward.svg", 32, 32));
        forwardButton.setBorderPainted(false);
        forwardButton.setBackground(null);
        forwardButton.setOpaque(false);
        forwardButton.setBackground(new Color(0, 0, 0, 0));
        forwardButton.setFocusable(false);
        forwardButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forwardButton.setContentAreaFilled(false);
        forwardButton.setOpaque(false);
        buttonPanel2.add(forwardButton);



        //volume slider
        volumeSlider = new JSlider(0,100,50);

        volumeSlider = new JSlider(0, 100, 50); // range from 0 (mute) to 100 (full volume)
        volumeSlider.setPreferredSize(new Dimension(150, 30));
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);

// When slider changes, update volume in MusicPlayer
        volumeSlider.addChangeListener(e -> {
            float volume = volumeSlider.getValue() / 100f;
            if (musicPlayer != null) {
                //musicPlayer.setVolume(volume);
            }
        });

        buttonPanel2.add(volumeSlider);


        buttonPanel.add(buttonPanel2, BorderLayout.CENTER);

        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private ImageIcon loadImage(String path) {
        try {
            // Try loading from assets folder first
            File imageFile = new File("assets/" + path);
            if (!imageFile.exists()) {
                imageFile = new File(path); // fallback to full path
            }

            BufferedImage img = ImageIO.read(imageFile);
            Image scaled = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            System.out.println("Failed to load image: " + path + " - " + e.getMessage());
            return createDefaultImage(300, 300); // Create placeholder
        }
    }

    private ImageIcon loadImage(String path, int width, int height) {
        try {
            File imageFile = new File("assets/" + path);
            if (!imageFile.exists()) {
                imageFile = new File(path);
            }

            BufferedImage img = ImageIO.read(imageFile);
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            System.out.println("Failed to load image: " + path + " - " + e.getMessage());
            return createDefaultImage(width, height);
        }
    }

    // Create placeholder when image fails to load
    private ImageIcon createDefaultImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(0,0,0,0));
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Image Not Found", 10, height/2);
        g2d.dispose();
        return new ImageIcon(img);
    }
}


public class ModernMusicPlayer {
    public static void main(String[] args) {
        FlatRobotoFont.install();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatMacDarkLaf.setup();

        EventQueue.invokeLater(()->{
            try {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
            } catch (UnsupportedLookAndFeelException e) {
                throw new RuntimeException(e);
            }
            new ModernGui();
        });
    }
}