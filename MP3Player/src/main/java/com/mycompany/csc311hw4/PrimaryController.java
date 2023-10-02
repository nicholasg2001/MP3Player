package com.mycompany.csc311hw4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PrimaryController {
    
    
    @FXML
    private AnchorPane anchorPane; //root container of GUI
    @FXML
    private Circle outerCircle; //outer circle of the speaker
    @FXML
    private Circle innerCircle; //inner circle of the speaker
    @FXML
    private Label songChosen; //informs the user of the selected song
    @FXML
    private Label currentTime; //run time of the song
    @FXML
    private Label totalTime; //duration of the song
    @FXML
    private Label showFilter; //show user which filter is applied to list view
    @FXML
    private ChoiceBox<String> filter;
    private final String[] filters = {"Title A-Z", "Artist A-Z", "Genre A-Z", "Duration"};
    @FXML
    private Button chooseSong; 
    @FXML
    private ListView listView;
    
    @FXML
    private Slider volume;
    @FXML
    private ProgressBar songProgress;
   
    private MediaPlayer mediaPlayer;
    private Media media;
    private Duration songDuration;
    private ParallelTransition speakerPlay;
    
    Map<Integer, Song> playlist = new HashMap<>();
    
    /**
     * Helper method to create the speaker "bumping" animation. 
     * @param circle 
     * @return returns the scale transition to be used for the speaker animation
     */
    public ScaleTransition animateSpeaker(Circle circle){
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(.5), circle);
        scaleTransition.setFromX(1);
        scaleTransition.setFromY(1);
        scaleTransition.setToX(1.5);
        scaleTransition.setToY(1.5);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
        return scaleTransition;
    }
    
    /**
     * Calls animateSpeaker() to prepare the speaker animation. Also assigns the animation to the global speakerPlay variable.
     * Adds a listener to the volume slider so the user can adjust the volume of the song.
     * Initializes the choice box and adds filter controls to the choice box items.
     */
    @FXML
    public void initialize(){
        //setup speaker animation
        ScaleTransition outer = animateSpeaker(outerCircle);
        ScaleTransition inner = animateSpeaker(innerCircle);
        speakerPlay = new ParallelTransition(outer, inner);
        //setup filter
        filter.getItems().addAll(filters);
        filter.setOnAction(event -> {
            String filterChoice = filter.getSelectionModel().getSelectedItem();
            if(filterChoice.equals("Title A-Z")){
                List<Song> filteredSongs = playlist.values()
                        .stream()
                        .sorted(Comparator.comparing(Song::getTitle))
                        .collect(Collectors.toList());
                listView.getItems().setAll(filteredSongs);
                showFilter.setText("Filtering by Title");
            }
            if(filterChoice.equals("Artist A-Z")){
                List<Song> filteredSongs = playlist.values()
                        .stream()
                        .sorted(Comparator.comparing(Song::getArtist))
                        .collect(Collectors.toList());
                listView.getItems().setAll(filteredSongs);
                showFilter.setText("Filtering by Artist");
            }
            if(filterChoice.equals("Genre A-Z")){
                List<Song> filteredSongs = playlist.values()
                        .stream()
                        .sorted(Comparator.comparing(Song::getGenre))
                        .collect(Collectors.toList());
                listView.getItems().setAll(filteredSongs);
                showFilter.setText("Filtering by Genre");
            }
            if(filterChoice.equals("Duration")){
                List<Song> filteredSongs = playlist.values()
                        .stream()
                        .sorted(Comparator.comparing(Song::getDurationInSeconds))
                        .collect(Collectors.toList());
                listView.getItems().setAll(filteredSongs);
                showFilter.setText("Filtering by Duration");
            }
        });
        //setup volume controls
        volume.valueProperty().addListener((ObservableValue<? extends Number> arg0, Number arg1, Number arg2) -> {
            mediaPlayer.setVolume(volume.getValue() * 0.01);
        });
    }
    /**
     * Displays an openFileDialog to the user, prompting them to select an MP3 file to play.
     * Updates the totalTime label with the duration of the song chosen.
     * Uses a listener to disable the chooseSong button when a song is playing. Pausing or stopping will re-enable it.
     */
    @FXML
    public void handleChooseSong(){
        FileChooser fileChooser = new FileChooser();
        File current = null;
        try{
            current = new File(new File("playlist").getCanonicalPath());
        }
        catch(IOException ex){
        
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));    
        }
        fileChooser.setInitialDirectory(current);
        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null){
            //setup media and media player 
            media = new Media(selectedFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            
            //add song name to label
            String songWithExtension = selectedFile.getName();
            int dotIndex = songWithExtension.lastIndexOf(".");
            String song = (dotIndex == -1) ? songWithExtension :songWithExtension.substring(0, dotIndex); //removes file extension
            songChosen.setText("Song Selected: " + song);
            
            //add song duration to label
            mediaPlayer.setOnReady(() -> {
                songDuration = media.getDuration();
                int minutes = (int) songDuration.toMinutes();
                int seconds = (int) songDuration.toSeconds() % 60;
                String durationString = String.format("%02d:%02d", minutes, seconds);
                totalTime.setText(durationString);
            });
            
            //disable choose song while a song is playing
            mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
                if (newStatus == MediaPlayer.Status.PLAYING) {
                    chooseSong.setDisable(true); 
                } else {
                    chooseSong.setDisable(false); 
                }
            });
        }
    }
    /**
     * Plays the selected song, updates song run time, and updates the progress bar to follow along with the song.
     */
    @FXML
    public void handlePlayButton(){
            if(media != null){
                //play song and animation
                mediaPlayer.play();
                speakerPlay.play();

                //create media timer
                mediaPlayer.currentTimeProperty().addListener((Observable ov) -> {

                //update runtime
                int minutes, seconds;
                minutes = (int) mediaPlayer.getCurrentTime().toMinutes();
                seconds = (int) mediaPlayer.getCurrentTime().toSeconds();
                String time = String.format("%02d:%02d", minutes, seconds % 60);
                currentTime.setText(time);

                //update progress slider
                songProgress.setProgress(seconds / songDuration.toSeconds());
                }); 
        } else{
            songChosen.setText("Song Selected: error no file selected");
        }
    }
    /**
     * Pauses the song
     */
    @FXML
    public void handlePauseButton(){
        if(media != null){
            mediaPlayer.pause();
            speakerPlay.pause();
        } else{
            songChosen.setText("Song Selected: error no file selected");
        }
    }
    /**
     * Restarts the selected song from the beginning
     */
    @FXML
    public void handleRestartButton(){
        if(media != null){
            mediaPlayer.seek(Duration.ZERO);
            songProgress.setProgress(0);
            mediaPlayer.play();
            speakerPlay.play();
        } else{
            songChosen.setText("Song Selected: error no file selected");
        }
    }
    /**
     * Stop button stops the mediaPlayer and removes the current loaded song from the MP3 player.
     */
    @FXML
    public void handleStopButton(){
        if(media != null){
            mediaPlayer.stop();
            speakerPlay.stop();
            currentTime.setText("0:00");
            media = null;
            songChosen.setText("Song cleared from media player");
        } else{
            songChosen.setText("Song Selected: error no file selected");
        }
    }
    /**
     * Populates the database with the playlist info from the JSON file.
     * Runs clearDB to clear the database of any previous entries before inserting data
     */
    @FXML
    public void handleLoadDbFromJson(){
        clearDB();
        FileChooser fileChooser = new FileChooser();
        File current = null;
        try{
            Connection conn = openConnection();
            String sql = "INSERT INTO musicPlaylist (ID, Title, Artist, Genre, Duration) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            try {
                try {
                    current = new File(new File(".").getCanonicalPath());
                } catch(IOException ex){
                    
                }
                fileChooser.setInitialDirectory(current);
                FileChooser.ExtensionFilter extFilter = 
                        new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json");
                fileChooser.getExtensionFilters().add(extFilter);
                File selectedFile = fileChooser.showOpenDialog(null);
                if(selectedFile != null){
                    FileReader fr = new FileReader(selectedFile);
                    Song[] musicPlaylist = gson.fromJson(fr, Song[].class);
                    for (Song song : musicPlaylist) {
                        preparedStatement.setInt(1, song.getID());
                        preparedStatement.setString(2, song.getTitle());
                        preparedStatement.setString(3, song.getArtist());
                        preparedStatement.setString(4, song.getGenre());
                        preparedStatement.setString(5, song.getDuration());
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (FileNotFoundException ex) {
            }
        } catch (SQLException e){
            
        }
        
    }
    /**
     * Exports playlist data (from hashMap) to a JSON file.
     */
    @FXML
    public void handleExportToJson(){
        FileChooser fileChooser = new FileChooser();
        File current = null;
        try{
            current = new File(new File(".").getCanonicalPath());
        } catch (IOException ex){
        }
        fileChooser.setInitialDirectory(current);
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showSaveDialog(null);
        
        if(selectedFile != null){
            try{
                FileWriter wr = new FileWriter("new playlist.json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                if(playlist.isEmpty() == false){
                    gson.toJson(playlist.values().stream()
                            .toArray(Song[]::new), wr);
                }
                wr.close();
                
            }
            catch(IOException e){
                
            }
        }
    }
     /**
     * Queries the DB to populate the hashMap (playlist) with song data
     */
    public void handleImportPlaylistFromDB(){
        Platform.runLater( () -> {
            playlist.clear(); //call clear to prevent duplicate entries
            Connection conn = openConnection();
            try{
                String tableName = "musicPlaylist";
                Statement getSong = conn.createStatement();
                ResultSet result = getSong.executeQuery("select * from " + tableName);

                while(result.next()){
                    Song song = new Song(result.getInt("ID"), result.getString("Title"),
                            result.getString("Artist"), result.getString("Genre"), 
                            result.getString("Duration"));
                    playlist.put(song.getID(), song);
                }
            }
            catch(SQLException e){

            }
        });
    } 
    
    /**
     * Calls handleImportPlaylistFromDB() on a separate thread
     * Populates the listView using Platform.runLater()
     */
    @FXML
    public void handleDisplayPlaylist(){
        Thread t = new Thread( () -> {
            handleImportPlaylistFromDB();
            Platform.runLater( () -> { 
                listView.getItems().clear();
                ObservableList<String> music = listView.getItems();
                if(playlist.isEmpty() == false){
                    for (Song song: playlist.values()){
                        music.add(song.toString());
                    }   
                }
                else{
                    //do nothing
                }
            });
        });
        t.start();
    }
  
    /**
     * Opens connection with MS Access playlist DB
     * @return connection variable to be used in other methods
     */
    public Connection openConnection(){
        String databaseURL = "";
        Connection conn = null;
        try{
            databaseURL = "jdbc:ucanaccess://.//PlaylistDB.accdb";
            conn = DriverManager.getConnection(databaseURL);
        } catch (SQLException ex) {
            
        }
        return conn;
    }
       
    /**
     * Clears listView of all playlist data, does not clear the playlist.
     */
    @FXML
    public void handleClearListView(){
        listView.getItems().clear();
    }
    
    /**
     * Clears MS access DB of all playlist data
     */
    @FXML
    public void clearDB(){
        Connection conn = openConnection();
        try{
            String sql = "DELETE FROM musicPlaylist";
            PreparedStatement clear = conn.prepareStatement(sql);
            clear.executeUpdate();
        }
        catch(SQLException e){
                
        }
    }
    
     /**
     * Closes the GUI when the menu button "close" is pressed.
     */
    @FXML
    public void handleCloseApp(){
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.close();
    }
    
    
}
