package controle;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import dao.DiretorioDAO;
import dao.MusicaDAO;
import dao.PlaylistDAO;
import excecoes.ExcecaoPersonalizada;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import modelo.Musica;
import modelo.Playlist;
import modelo.UsuarioVIP;
import util.Alertas;
import util.GerenciadorCenas;

public class TelaPrincipalController implements Initializable {
	
	@FXML
	private ImageView icone;
	
	@FXML
	private Label tituloPlaylist;
	
	@FXML
    private ProgressBar progressoMusica;

    @FXML
    private ListView<Musica> listaMusicas;
    
    @FXML
    private Button btPlaylist;
    
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
    private ListView<Playlist> listaPlaylists;
    
    @FXML
    private BorderPane telaPrincipal;
    
    private static TelaPrincipalController instance = new TelaPrincipalController();
    
    private Playlist playlistAtual;
    
    //-----------------------
    
    private Timer timer;
	private TimerTask task;
	private boolean running = false;
	private File file;
	private Media media;
	private MediaPlayer mediaPlayer;
	private int indexMusicaGeral;
    private boolean controlePrimeiraMusica;
    
    
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
    	DiretorioDAO.carregar(TelaLoginController.getInstance().getUsuarioAtual());
    	atualizarMusicas();
    	
		ContextMenu menu = menuMusica();
		listaMusicas.setContextMenu(menu);
		
		 if (TelaLoginController.getInstance().getUsuarioAtual() instanceof UsuarioVIP) {
			btPlaylist.setVisible(true);
			tituloPlaylist.setVisible(true);
		    listaPlaylists.setVisible(true);
		    menu = menuPlaylist();
			listaPlaylists.setContextMenu(menu);
			
			instance.listaPlaylists = listaPlaylists;
			
			atualizarPlaylists();
			
			listaPlaylists.setOnMouseClicked(event -> {
	            if (event.getClickCount() == 2 && !listaPlaylists.getSelectionModel().isEmpty()) {
	                Playlist playlistSelecionada = listaPlaylists.getSelectionModel().getSelectedItem();
	                instance.playlistAtual = playlistSelecionada;
	                GerenciadorCenas.mudarCena("../visao/TelaPlaylist.fxml");
	            }
	        });
			
	    } else {
	    	listaPlaylists.setVisible(false);
	    	btPlaylist.setVisible(false);
	    	tituloPlaylist.setVisible(false);
	    }
		 
		 instance.listaMusicas = listaMusicas;
		 
		 if ( getListaMusicaItems() != null && !getListaMusicaItems().isEmpty()) {
			 file = new File(getListaMusicaItems().get(0).getCaminhoArquivo());
			 media = new Media(file.toURI().toString());
			 mediaPlayer = new MediaPlayer(media);
			 indexMusicaGeral = 0;
			 controlePrimeiraMusica = true;
		 }else {
			 controlePrimeiraMusica = false;
		 }
		 
		 listaMusicas.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && !listaMusicas.getSelectionModel().isEmpty() ) {
				indexMusicaGeral = getListaMusicaItems().indexOf(listaMusicas.getSelectionModel().getSelectedItem());
				mediaPlayer.stop();
				if(running) cancelTimer();
				file = new File(getListaMusicaItems().get(indexMusicaGeral).getCaminhoArquivo());
				media = new Media(file.toURI().toString());
				mediaPlayer = new MediaPlayer(media);
				if(!controlePrimeiraMusica) controlePrimeiraMusica = true;
				playMedia();
			}
			 
		 });
	}
    
	private void atualizarMusicas() {
		ArrayList<Musica> musicasCarregadas = MusicaDAO.carregar(TelaLoginController.getInstance().getUsuarioAtual());
		listaMusicas.getItems().setAll(musicasCarregadas);
	}
	
	public void atualizarPlaylists() {
		ArrayList<Playlist> playlistsCarregadas = PlaylistDAO.carregar((UsuarioVIP) TelaLoginController.getInstance().getUsuarioAtual());
		listaPlaylists.getItems().setAll(playlistsCarregadas);
	}
	
	@FXML
	private void iconeAcao() {
		ContextMenu menu = menuUsuario();
		configuraClique(icone,menu);
		
	}
	
    protected ContextMenu menuUsuario() {
        ContextMenu menu = new ContextMenu();

        MenuItem opcao1 = new MenuItem("Conta");
        opcao1.setOnAction(event -> {
        	GerenciadorCenas.mudarCena("../visao/TelaConta.fxml");
        });

        MenuItem opcao2 = new MenuItem("Sair");
        opcao2.setOnAction(event -> {
            GerenciadorCenas.mudarCena("../visao/TelaLogin.fxml");
        });
        
        menu.getItems().addAll(opcao1, opcao2);

        return menu;
    }
    
    protected ContextMenu menuMusica() {
    	ContextMenu menu = new ContextMenu();
    	
    	MenuItem removerItem = new MenuItem("Remover");
    	menu.getItems().add(removerItem);

    	removerItem.setOnAction(event -> {
    	    Musica musicaSelecionada = listaMusicas.getSelectionModel().getSelectedItem();
    	    if (musicaSelecionada != null) {
    	    	removerMusica(musicaSelecionada);
    	    }
    	});
        

        return menu;
    }
    
    private ContextMenu menuPlaylist() {
    	ContextMenu menu = new ContextMenu();
    	
    	MenuItem removerItem = new MenuItem("Remover");
    	menu.getItems().add(removerItem);
    	
    	removerItem.setOnAction(event -> {
    	    Playlist playlistSelcionada = listaPlaylists.getSelectionModel().getSelectedItem();
    	    if (playlistSelcionada != null) {
    	    	removerPlaylist(playlistSelcionada);
    	    }
    	});
    	
    	MenuItem editarItem = new MenuItem("Editar");
    	menu.getItems().add(editarItem);

    	editarItem.setOnAction(event -> {
    	    Playlist playlistSelcionada = listaPlaylists.getSelectionModel().getSelectedItem();
    	    if (playlistSelcionada != null) {
    	    	editarPlaylist(playlistSelcionada);
    	    }
    	});
        

        return menu;
    }
    
    
    private void removerMusica(Musica musicaSelecionada) {
    	MusicaDAO.remover(TelaLoginController.getInstance().getUsuarioAtual(), musicaSelecionada);
        listaMusicas.getItems().remove(musicaSelecionada);

        
    }
    
    private void removerPlaylist(Playlist playlistSelecionada) {
    	PlaylistDAO.remover(playlistSelecionada, (UsuarioVIP) TelaLoginController.getInstance().getUsuarioAtual());
        listaPlaylists.getItems().remove(playlistSelecionada);
        
    }
    
    private void editarPlaylist(Playlist playlistSelecionada) 
    {
    	instance.playlistAtual = playlistSelecionada;
    	GerenciadorCenas.abrirNovaJanela("/visao/TelaEditarPlaylist.fxml");
        
    }

    protected void configuraClique(Node objeto, ContextMenu menu) {
       objeto.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                menu.show(objeto, event.getScreenX(), event.getScreenY());
            }
        });
    }
	
	@FXML
	private void adicionarDiretorioAcao() {
        File diretorio = escolherDiretorio();
  
        if(diretorio != null) {
	        try {
	           DiretorioDAO.adicionar(TelaLoginController.getInstance().getUsuarioAtual(), diretorio.getAbsolutePath());
	           atualizarMusicas();
	        } catch (ExcecaoPersonalizada e) {
	            Alertas.showAlert("Erro", e.getMessage(), "", Alert.AlertType.ERROR);
	        } catch (Exception e) {
	            Alertas.showAlert("Erro", "Erro ao remover a música.", e.getMessage(), Alert.AlertType.ERROR);
	        } 
        }
	}
	
	@FXML
	public void removerDiretorioAcao() {
        File diretorio = escolherDiretorio();
        
       if(diretorio != null) {
    	   DiretorioDAO.remover(diretorio.getAbsolutePath(), TelaLoginController.getInstance().getUsuarioAtual());
           atualizarMusicas();
       }
	}
	
    private File escolherDiretorio() {
    	DirectoryChooser buscaDiretorio = new DirectoryChooser();
    	buscaDiretorio.setTitle("Selecionar Diretório");
        return buscaDiretorio.showDialog(null);
    }
    
    private File escolherArquivo() {
    	FileChooser buscaArquivo = new FileChooser();
    	buscaArquivo.setTitle("Selecionar Música");
    	buscaArquivo.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Arquivos MP3", "*.mp3")
        );
    	return buscaArquivo.showOpenDialog(null);
    }
    
    @FXML
    private void adicionarMusicaAcao() {
  
        File arquivo = escolherArquivo();
        
        if(arquivo != null) {
        	
        	Musica novaMusica = new Musica(arquivo.getName(),arquivo.getAbsolutePath());
            MusicaDAO.adicionar(TelaLoginController.getInstance().getUsuarioAtual(), novaMusica);
            if(!listaMusicas.getItems().contains(novaMusica)) {
            	listaMusicas.getItems().add(novaMusica);
            }
        }
    }
    
    @FXML
    private void adicionarPlaylistAcao() {
    	GerenciadorCenas.abrirNovaJanela("/visao/TelaAdicionarPlaylist.fxml");
    }
    
	public static TelaPrincipalController getInstance() {
		return instance;
	}
	
	public Playlist getPlaylistAtual() {
		return playlistAtual;
	}
	
	public ObservableList<Musica> getListaMusicaItems() {
        return listaMusicas.getItems();
    }
	

	public void playMedia() {
		beginTimer();
		mediaPlayer.play();
	}
	
	@FXML
	public void playMediaGeral() {
		if (controlePrimeiraMusica) {
			playMedia();
		}else {
			file = new File(getListaMusicaItems().get(0).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			indexMusicaGeral = 0;
			controlePrimeiraMusica = true;
			playMedia();
		}
	}
	
	public void stopMediaGeral() {
		mediaPlayer.stop();
		if(running) cancelTimer();
		
		file = new File(getListaMusicaItems().get(0).getCaminhoArquivo());
		media = new Media(file.toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		indexMusicaGeral = 0;
	}
	
	@FXML
	public void pauseMedia() {
		cancelTimer();
		mediaPlayer.pause();
	}
	
	@FXML
	public void nextMediaGeral() {
		if(indexMusicaGeral < getListaMusicaItems().size() - 1) {			
			indexMusicaGeral++;
			mediaPlayer.stop();
			if(running) cancelTimer();
			
			file = new File(getListaMusicaItems().get(indexMusicaGeral).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}
		else {
			indexMusicaGeral = 0;
			mediaPlayer.stop();
			
			file = new File(getListaMusicaItems().get(indexMusicaGeral).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}
	}
	
	@FXML
	public void previousMediaGeral() {
		if (indexMusicaGeral == 0) {
			indexMusicaGeral = getListaMusicaItems().size() - 1;
			mediaPlayer.stop();
			if(running) cancelTimer();
			
			file = new File(getListaMusicaItems().get(indexMusicaGeral).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}else {
			indexMusicaGeral--;
			mediaPlayer.stop();
			if(running) cancelTimer();
			
			file = new File(getListaMusicaItems().get(indexMusicaGeral).getCaminhoArquivo());
			media = new Media(file.toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			
			playMedia();
		}
	}
	
	public void muteMedia() {
	    if (mediaPlayer != null) {
	        mediaPlayer.setMute(!mediaPlayer.isMute()); // Inverte o estado de mudo (mute)
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
					Platform.runLater(() -> nextMediaGeral());
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

