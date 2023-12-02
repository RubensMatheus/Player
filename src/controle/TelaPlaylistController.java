package controle;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import dao.PlaylistDAO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import modelo.Musica;
import modelo.UsuarioVIP;
import util.GerenciadorCenas;

public class TelaPlaylistController implements Initializable {
	
    @FXML
    private Button btPlay;
    
    @FXML
    private Button btAnterior;
    
    @FXML
    private Button btProximo;
    
    @FXML
    private Button btPause;
    
    @FXML
    private Button btMutar;
    
    @FXML
    private Button btParar;
    
    @FXML
    private Button btAdicionarMusica;
    
    @FXML
    private Button btRemoverMusica;
    
    @FXML
    private Button btVoltar; 
    
    @FXML
    private Label nomePlaylist; 
    
	@FXML
    private ProgressBar progressoMusica;
	
	@FXML
	private ListView<Musica> listaMusicas;
	
	@FXML
	private ListView<Musica> listaMusicasPlaylist;
	
	
    @FXML
    private BorderPane telaPlaylist;
    
    
    private Timer timer;
	private TimerTask task;
	private boolean running = false;
	private File file;
	private Media media;
	private MediaPlayer mediaPlayer;
	private int indexMusicaPlaylist;
    private boolean controlePrimeiraMusica;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		ObservableList<Musica> musicas = TelaPrincipalController.getInstance().getListaMusicaItems();
        listaMusicas.setItems(musicas);
        
        nomePlaylist.setText(TelaPrincipalController.getInstance().getPlaylistAtual().getNome());
        
        atualizarMusicasPlaylist();
        
        if ( getListaMusicaPlaylist() != null && !getListaMusicaPlaylist().isEmpty()) {
			 file = new File(getListaMusicaPlaylist().get(0).getCaminhoArquivo());
			 media = new Media(file.toURI().toString());
			 mediaPlayer = new MediaPlayer(media);
			 indexMusicaPlaylist = 0;
			 controlePrimeiraMusica = true;
		 }else {
			 controlePrimeiraMusica = false;
		 }
        
        listaMusicasPlaylist.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && !listaMusicasPlaylist.getSelectionModel().isEmpty() ) {
				indexMusicaPlaylist = getListaMusicaPlaylist().indexOf(listaMusicasPlaylist.getSelectionModel().getSelectedItem());
				mediaPlayer.stop();
				if(running) cancelTimer();
				file = new File(getListaMusicaPlaylist().get(indexMusicaPlaylist).getCaminhoArquivo());
				media = new Media(file.toURI().toString());
				mediaPlayer = new MediaPlayer(media);
				if(!controlePrimeiraMusica) controlePrimeiraMusica = true;
				playMedia();
			}
			 
		 });
		
	}
	
	private void atualizarMusicasPlaylist() {
		ArrayList<Musica> musicasCarregadas = TelaPrincipalController.getInstance().getPlaylistAtual().getMusicas();
		listaMusicasPlaylist.getItems().setAll(musicasCarregadas); 
	}
	
	@FXML
	private void btAdicionarMusicaAcao() {
		Musica musicaSelecionada = listaMusicas.getSelectionModel().getSelectedItem();
        if (musicaSelecionada != null && !listaMusicasPlaylist.getItems().contains(musicaSelecionada)) {
            listaMusicasPlaylist.getItems().add(musicaSelecionada);
            PlaylistDAO.adicionarMusica(musicaSelecionada, 
            							TelaPrincipalController.getInstance().getPlaylistAtual(), 
            							(UsuarioVIP) TelaLoginController.getInstance().getUsuarioAtual());
        }
	}
	
	@FXML
	private void btRemoverMusicaAcao() {
		Musica musicaSelecionada = listaMusicasPlaylist.getSelectionModel().getSelectedItem();
        if (musicaSelecionada != null) {
            listaMusicasPlaylist.getItems().remove(musicaSelecionada);
            PlaylistDAO.removerMusica(musicaSelecionada, (UsuarioVIP) TelaLoginController.getInstance().getUsuarioAtual());
        }
	}
	
	@FXML
	private void btVoltarAcao() {
		GerenciadorCenas.mudarCena("../visao/TelaPrincipal.fxml");
	}
	
	
	public ObservableList<Musica> getListaMusicaPlaylist() {
        return listaMusicasPlaylist.getItems();
    }
	
	public void playMedia() {
		beginTimer();
		mediaPlayer.play();
	}
	
	@FXML
	public void playMediaPlaylist() {
		if (controlePrimeiraMusica) {
			playMedia();
		}else {
			file = new File(getListaMusicaPlaylist().get(0).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			indexMusicaPlaylist = 0;
			controlePrimeiraMusica = true;
			playMedia();
		}
	}
	
	public void stopMediaPlaylist() {
		mediaPlayer.stop();
		if(running) cancelTimer();
		
		file = new File(getListaMusicaPlaylist().get(0).getCaminhoArquivo());
		media = new Media(file.toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		indexMusicaPlaylist = 0;
	}
	
	@FXML
	public void pauseMedia() {
		cancelTimer();
		mediaPlayer.pause();
	}
	
	@FXML
	public void nextMediaPlaylist() {
		System.out.println("entrou");
		if(indexMusicaPlaylist < getListaMusicaPlaylist().size() - 1) {			
			indexMusicaPlaylist++;
			mediaPlayer.stop();
			if(running) cancelTimer();
			
			file = new File(getListaMusicaPlaylist().get(indexMusicaPlaylist).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}
		else {
			indexMusicaPlaylist = 0;
			mediaPlayer.stop();
			
			file = new File(getListaMusicaPlaylist().get(indexMusicaPlaylist).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}
	}
	
	@FXML
	public void previousMediaPlaylist() {
		if (indexMusicaPlaylist == 0) {
			indexMusicaPlaylist = getListaMusicaPlaylist().size() - 1;
			mediaPlayer.stop();
			if(running) cancelTimer();
			
			file = new File(getListaMusicaPlaylist().get(indexMusicaPlaylist).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}else {
			indexMusicaPlaylist--;
			mediaPlayer.stop();
			if(running) cancelTimer();
			
			file = new File(getListaMusicaPlaylist().get(indexMusicaPlaylist).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}
	}
	
	public void muteMedia() {
	    if (mediaPlayer != null) {
	        mediaPlayer.setMute(!mediaPlayer.isMute());
	    }
	}

	
	public void beginTimer() {
		timer = new Timer();
		task = new TimerTask() {
			
			public void run() {
				running = true;
				double current = mediaPlayer.getCurrentTime().toSeconds();
				double end = media.getDuration().toSeconds();
				progressoMusica.setProgress(current/end);
				if(current/end >= 1) {
					Platform.runLater(() -> nextMediaPlaylist());
				}
			}
		};
		
		timer.scheduleAtFixedRate(task, 0, 1000);
	}
	
	public void cancelTimer() {
		running = false;
		timer.cancel();
	}

}