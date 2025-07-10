package org.lms;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
/* these music playlist dialog extends jDialog because playlist will be ccreated and saved using this dialoag box using array list,file writer buffered writer
*/
public class MusicPlaylistDialog extends JDialog {
    private ModernGui modernGui;
    private ArrayList<String> songPath;
    public MusicPlaylistDialog(ModernGui modernGui) {
        this.modernGui = modernGui;

        songPath =  new ArrayList<>();

        setTitle("Music Playlist");
        setSize(400,400);
        setResizable(false);
        getContentPane().setBackground(ModernGui.FRAME_COLOR);
        setLayout(null);
        setLocationRelativeTo(null);
        setModal(true);

        addDialogComponents();
    }
    private void addDialogComponents() {
        JPanel songContainer =  new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int)(getWidth() * 0.025), 10, (int)(getWidth() * 0.90), (int)(getHeight() * 0.75));
        add(songContainer);


        JButton addSong = new JButton("Add Song");
        addSong.setBounds(60, (int)(getHeight() * 0.80), 100, 25);
        addSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.music", System.getProperty("user.home", ".") + "/Music")));
                fileChooser.setAcceptAllFileFilterUsed(false);
                int result =  fileChooser.showOpenDialog(MusicPlaylistDialog.this);

                File selectedFile = fileChooser.getSelectedFile();
                //this check will search if there are any file other than (.mp3) extension if yes it reuturn back to dialog while showing dialog message
                if(selectedFile != null && result ==  JFileChooser.APPROVE_OPTION) {

                    String fileName = selectedFile.getName().toLowerCase();
                    if (!fileName.endsWith(".mp3")) {
                        JOptionPane.showMessageDialog(MusicPlaylistDialog.this,
                                "Please select only MP3 files!",
                                "Invalid File Type",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // fallback if reading a file gives error or if mp3 file doesn't contain bytes or inside of it pr empty
                    if (!selectedFile.exists() || !selectedFile.canRead()) {
                        JOptionPane.showMessageDialog(MusicPlaylistDialog.this,
                                "Cannot read the selected file!",
                                "File Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //file path label will show us text/title of song in dialog box and border are used to distinguish between song title
                    JLabel filePathLabel = new  JLabel(selectedFile.getAbsolutePath());
                    filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    songPath.add(filePathLabel.getText());

                    songContainer.add(filePathLabel);

                    //refreshers song container when song is added
                    songContainer.revalidate();

                }
            }
        });
        add(addSong);

        JButton savePlst = new JButton("Save");
        savePlst.setBounds(215, (int)(getHeight() * 0.80), 100, 25);
        //lets you save the added song with modifiable name of your file in .txt
        savePlst.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    //this will open in the directory where you save the file
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.music", System.getProperty("user.home", ".") + "/Music")));
                    int result =  fileChooser.showSaveDialog(MusicPlaylistDialog.this);

                    //the checkpost will help  type only playlist name save the hassle for typing .txt
                    if(result == fileChooser.APPROVE_OPTION){

                        File selectedFile = fileChooser.getSelectedFile();

                        String fileName = selectedFile.getName();
                        if (!fileName.toLowerCase().endsWith(".txt")) {
                            selectedFile = new File(selectedFile.getParent(), fileName + ".txt");
                        }

                        if(!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")){
                            selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                        }
                        selectedFile.createNewFile();

                        FileWriter fileWriter = new FileWriter(selectedFile);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        for(String sngPath : songPath){
                            bufferedWriter.write(sngPath + "\n");
                        }
                        bufferedWriter.close();

                        JOptionPane.showMessageDialog(MusicPlaylistDialog.this, "Successfully Created Playlist!");

                        MusicPlaylistDialog.this.dispose();
                        //using dispose method to save up memory
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        add(savePlst);

    }
}