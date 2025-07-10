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
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSliderUI;

class ModernGui extends Component {

    static final Color FRAME_COLOR = Color.BLACK;
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
    File selectedFile;

    public ModernGui() {
        preloadDefaultImages(); // Load default images once at startup
        initializeUI();
        musicPlayer = new MusicPlayer(this);
    }
    
    //this method will set default images when no song is loaded or when song has no metadata
    private void preloadDefaultImages() {
        try {
            this.defaultLogo = loadImageFromFile("E:/musicapp/modern-music-player/src/main/resources/com/myapp/icons/logo.png", 300, 300);
            this.defaultBackground = loadImageFromFile("E:/musicapp/modern-music-player/src/main/resources/com/myapp/icons/backgroundimage.png", 600, 600);

            if (defaultLogo == null) {
                System.err.println("Warning: Could not load default logo");
                defaultLogo = createDefaultImage(300, 300);
            }
            if (defaultBackground == null) {
                System.err.println("Warning: Could not load default background");
                defaultBackground = createDefaultImage(600, 600);
            }
        } catch (Exception e) {
            System.err.println("Error preloading default images: " + e.getMessage());
            e.printStackTrace();
            defaultLogo = createDefaultImage(300, 300);
            defaultBackground = createDefaultImage(600, 600);
        }
    }

    //setting custom setting/size of image - ( used for default images )
    private ImageIcon loadImageFromFile(String path, int width, int height) {
        try {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (IOException e) {
            System.out.println("Failed to load image: " + path + " - " + e.getMessage());
        }
        return null;
    }

    // this method initializes the frame and calling method that contain other components
    private void initializeUI() {
        frame = new JFrame("Music Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(600, 600);
        backgroundPanel = new BackgroundPanel(defaultBackground);
        backgroundPanel.setLayout(new BorderLayout());
        frame.setContentPane(backgroundPanel);
        frame.setLocationRelativeTo(null);
        ImageIcon icon = new ImageIcon(getClass().getResource("/com/myapp/icons/logo.png"));
        Image scaledImage = icon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
        frame.setIconImage(scaledImage);
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP3 Files", "mp3"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.music", System.getProperty("user.home", ".") + "/Music")));
        fileChooser.setAcceptAllFileFilterUsed(false);

        musicPlayer = new MusicPlayer(this);
        setupToolBar(fileChooser);
        setupCenterPanel();
        setupControlButtons();

        frame.setResizable(false);
        frame.setVisible(true);
    }

    /*BackgroundPanel class is used to define background Image and its setting and it is used to set the opacity of background image
    *and alpha is used to control it's opacity the higher the alpha the more bright images will be
     */

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        private float alpha = 0.4f; // Default alpha

        // Modified constructor to accept ImageIcon
        public BackgroundPanel(ImageIcon imageIcon) {
            if (imageIcon != null) {
                this.backgroundImage = imageIcon.getImage();
            }
        }

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

    //toolbar to combine load song and playlist functionality

    private void setupToolBar(JFileChooser fileChooser) {

        //main toolbar panel in which next components are added
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
        toolbar.setOpaque(false);


        //menuBar is used to hold songMenu component
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        menuBar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuBar.setOpaque(false);
        menuBar.setBackground(new Color(0,0,0,0));


        JButton songMenu = new JButton();
        songMenu.setFocusable(false);
        songMenu.setBackground(new Color(0,0,0,0));
        songMenu.setIcon(new FlatSVGIcon("com/myapp/icons/music.svg", 24, 24));
        songMenu.setToolTipText("Songs");
        songMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                songMenu.setBackground(new Color(0x383838));
            }
            public void mouseExited(MouseEvent e) {
                songMenu.setBackground(new Color(0x0383838, true));
            }
        });

        songMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(frame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    
                    //while the thread is loading, these will be our indicator text
                    songTitle.setText("Loading...");
                    songArtist.setText("Please wait...");

                    // loading this song in another thread will stop freezing when update Song artwork cannot find metadata of a song
                    SwingUtilities.invokeLater(() -> {
                        song = new Song(selectedFile.getAbsolutePath());
                        
                        // Update UI
                        updateSongArtwork(song);
                        updatetitles(song);


                        if (musicPlayer != null) {
                            musicPlayer.stopSong();
                        }
                        if (musicPlayer == null) {
                            musicPlayer = new MusicPlayer(ModernGui.this);
                        }

                        musicPlayer.loadSong(song);
                        updatePlaybackSlider(song);
                        playerSlider.setValue(0);
                        enablePauseButtonandDisablePlayButton();
                    });
                }
            }
        });
        menuBar.add(songMenu);

        // Playlist Menu with SVG icon (icon-only, no text)
        JMenu playlistMenu = new JMenu();
        playlistMenu.setOpaque(false);
        playlistMenu.setFocusable(false);
        playlistMenu.setIcon(new FlatSVGIcon("com/myapp/icons/playlist.svg", 24, 24));
        playlistMenu.setToolTipText("Playlist");

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.setIcon(new FlatSVGIcon("com/myapp/icons/plus.svg", 16, 16));
        createPlaylist.setToolTipText("Create Playlist");

        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MusicPlaylistDialog(ModernGui.this).setVisible(true);
            }
        });

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.setIcon(new FlatSVGIcon("com/myapp/icons/load.svg", 16, 16));
        loadPlaylist.setToolTipText("Load Playlist");

        loadPlaylist.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Playlist Text Files", "txt"));
            chooser = new JFileChooser(new File(System.getProperty("user.home") + "/Music"));
            chooser.setFileFilter(new FileNameExtensionFilter("Text Playlist Files (*.txt)", "txt"));
            chooser.setAcceptAllFileFilterUsed(false);

            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                loadPlaylistFromFile(selectedFile);
            }
        });

        playlistMenu.add(createPlaylist);
        playlistMenu.add(loadPlaylist);
        menuBar.add(playlistMenu);

        toolbar.add(menuBar);
        frame.add(toolbar, BorderLayout.NORTH);
    }

    private void loadPlaylistFromFile(File file) {

        //array list is used to add song paths
        ArrayList<String> paths = new ArrayList<>();

        //read all the file paths
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                paths.add(line.trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to load playlist.");
            return;
        }

        if (paths.isEmpty()) {
            System.out.println("Playlist is empty.");
            return;
        }

        musicPlayer.loadPlaylistFromPaths(paths);
        this.song = musicPlayer.getCurrentSong();
        updatetitles(song);
        updateSongArtwork(song);
        updatePlaybackSlider(song);
        playerSlider.setValue(0);
        enablePauseButtonandDisablePlayButton();
        System.out.println("Playlist loaded successfully!");
    }

    //these will help us control play/pause buttons when no song is loaded
    public void enablePauseButtonandDisablePlayButton(){
        if (playButton != null && pauseButton != null) {
            playButton.setVisible(false);
            playButton.setEnabled(false);

            pauseButton.setVisible(true);
            pauseButton.setEnabled(true);
        }
    }
    public void enablePlayButtonandDisablePauseButton(){
        if (playButton != null && pauseButton != null) {
            playButton.setVisible(true);
            playButton.setEnabled(true);

            pauseButton.setVisible(false);
            pauseButton.setEnabled(false);
        }
    }

    //this song will update titles and artist name if available
    public void updatetitles(Song song){
        this.song = song;
        if (this.song != null) {
            songTitle.setText(this.song.getSongTitle());
            songArtist.setText(this.song.getSongArtist());
        }
    }

    //set slider value to the songs total frame
    public void setPlayerSliderValue(int frame){
        playerSlider.setValue(frame);
    }

    //the main centerPanel used to show song artwork, title, and artist name while showing the background image
    private void setupCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Song image use preloaded default
        songImage = new JLabel(defaultLogo);
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
        //this is used to set Custom basic slider and rendering its length and UI and color
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
        playerSlider.setPreferredSize(new Dimension(300, 50));
        playerSlider.setMaximumSize(new Dimension(300, 50));
        playerSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerSlider.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (musicPlayer != null) {
                    musicPlayer.pauseSong();
                }
            }
            public void mouseReleased(MouseEvent e) {
                if (musicPlayer != null && song != null) {
                    JSlider source = (JSlider) e.getSource();
                    int clickedFrame = source.getValue();

                    int timeInMilliseconds = (int) (clickedFrame / song.getFrameRatePerMiliSeconds());

                    musicPlayer.setCurrentFrame(clickedFrame);
                    musicPlayer.setCurrentTimeinMili(timeInMilliseconds);

                    playerSlider.setValue(clickedFrame);
                    musicPlayer.playCurrentSong();
                    enablePauseButtonandDisablePlayButton();
                }
            }
        });

        // Add to contentPanel
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(songImage);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(songTitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(songArtist);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(playerSlider);
        contentPanel.add(Box.createVerticalGlue());

        centerPanel.add(contentPanel, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);
    }

    //used where song current song is replaced and will set the slider to the total frame of that song using frame countt
    public void updatePlaybackSlider(Song song){
        if (song == null || song.getMp3File() == null) return;
        
        playerSlider.setMaximum(song.getMp3File().getFrameCount());
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>(); //  this structure uses a key to identify and set beginning and ending of the song
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
    }
    

    public void updateSongArtwork(Song song) {
        try {
            if (song != null) {
                ImageIcon artwork = song.getSongArtwork();
                if (artwork != null) {
                    //update the current song to is artwork if availaible
                    songImage.setIcon(artwork);
                    backgroundPanel.setBackgroundImage(artwork);
                } else {
                    //fallback images
                    songImage.setIcon(defaultLogo);
                    backgroundPanel.setBackgroundImage(defaultBackground);
                }
            } else {
                // No song loaded - use preloaded defaults
                songImage.setIcon(defaultLogo);
                backgroundPanel.setBackgroundImage(defaultBackground);
            }

            //refreshes the images
            songImage.repaint();
            backgroundPanel.repaint();

        } catch (Exception e) {
            System.err.println("Error updating song artwork: " + e.getMessage());
            e.printStackTrace();

            songImage.setIcon(defaultLogo);
            backgroundPanel.setBackgroundImage(defaultBackground);
        }
    }

    //main play bak functionality of button
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
        prevButton.setBackground(new Color(0, 0, 0, 0));
        prevButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                prevButton.setBackground(new Color(0x383838));
            }
            public void mouseExited(MouseEvent e) {
                prevButton.setBackground(new Color(0x0383838, true));
            }
        });
        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.previousSong();
            }
        });
        buttonPanel2.add(prevButton);

        playButton = new JButton(new FlatSVGIcon("com/myapp/icons/play.svg", 32,32));
        playButton.setBorderPainted(false);
        playButton.setFocusable(false);
        playButton.setBackground(new Color(0, 0, 0, 0));
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playButton.setBackground(new Color(0x383838));
            }
            public void mouseExited(MouseEvent e) {
                playButton.setBackground(new Color(0x0383838, true));
            }
        });
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
        pauseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pauseButton.setBackground(new Color(0, 0, 0, 0));
        pauseButton.setFocusable(false);
        pauseButton.setVisible(false);
        pauseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pauseButton.setBackground(new Color(0x383838));
            }
            public void mouseExited(MouseEvent e) {
                pauseButton.setBackground(new Color(0x0383838, true));
            }
        });
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
        forwardButton.setBackground(new Color(0, 0, 0, 0));
        forwardButton.setFocusable(false);
        forwardButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.playNextSong();
            }
        });
        forwardButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forwardButton.setBackground(new Color(0x383838));
            }
            public void mouseExited(MouseEvent e) {
                forwardButton.setBackground(new Color(0x0383838, true));
            }
        });
        buttonPanel2.add(forwardButton);

        //volume slider
        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setPreferredSize(new Dimension(150, 30));
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);

        volumeSlider.addChangeListener(e -> {
            float volume = volumeSlider.getValue() / 100f;
        });
        
        JPanel wp = new JPanel(new BorderLayout());
        wp.setOpaque(false);
        wp.setPreferredSize(new Dimension(130, 70));
        buttonPanel.add(wp, BorderLayout.WEST); //  this panel centers the main platbac kbuttons
        buttonPanel.add(buttonPanel2, BorderLayout.CENTER);
        buttonPanel.add(volumeSlider, BorderLayout.EAST);
        frame.add(buttonPanel, BorderLayout.SOUTH);
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
//main method uses flatmacdarklaf for better gui
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