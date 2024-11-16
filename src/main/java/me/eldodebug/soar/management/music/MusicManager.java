package me.eldodebug.soar.management.music;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
import me.eldodebug.soar.management.mods.impl.MusicInfoMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.music.ytdlp.Ytdlp;
import me.eldodebug.soar.mp3agic.Mp3File;
import me.eldodebug.soar.mp3agic.interfaces.ID3v2;
import me.eldodebug.soar.utils.ImageUtils;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.RandomUtils;
import me.eldodebug.soar.utils.file.FileUtils;

public class MusicManager {

	private CopyOnWriteArrayList<Music> musics = new CopyOnWriteArrayList<Music>();
	private Ytdlp ytdlp = new Ytdlp();
	
	private Music currentMusic;
	private Media media;
	private MediaPlayer mediaPlayer;
	
	public MusicManager() {
		load();
		loadData();
	}
	
	public void loadData() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		File cacheDir = new File(fileManager.getCacheDir(), "music");
		File dataJson = new File(cacheDir, "Data.json");
		
		ArrayList<String> favorites = new ArrayList<String>();
		
		if(!dataJson.exists()) {
			fileManager.createFile(dataJson);
		}
		
		try (FileReader reader = new FileReader(dataJson)) {
			
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
			
			if(jsonObject != null) {
				
				JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Favorite Musics");
				
				if(jsonArray != null) {
					
					Iterator<JsonElement> iterator = jsonArray.iterator();
					
					while(iterator.hasNext()) {
						
						JsonElement jsonElement = (JsonElement) iterator.next();
						JsonObject rJsonObject = gson.fromJson(jsonElement, JsonObject.class);
						
						favorites.add(JsonUtils.getStringProperty(rJsonObject, "Favorite", "null"));
					}
				}
			}
		} catch (Exception e) {}
		
		for(Music m : musics) {
			if(favorites.contains(m.getName())) {
				m.setType(MusicType.FAVORITE);
			}
		}
	}
	
	public void saveData() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		File cacheDir = new File(fileManager.getCacheDir(), "music");
		File dataJson = new File(cacheDir, "Data.json");
		
		if(!dataJson.exists()) {
			fileManager.createFile(dataJson);
		}
		
		try(FileWriter writer = new FileWriter(dataJson)) {
			
			JsonObject jsonObject = new JsonObject();
			JsonArray jsonArray = new JsonArray();
			Gson gson = new Gson();
			
			for(Music m : musics) {
				
				if(m.getType().equals(MusicType.FAVORITE)) {
					
					JsonObject innerJsonObject = new JsonObject();
					
					innerJsonObject.addProperty("Favorite", m.getName());
					
					jsonArray.add(innerJsonObject);
				}
			}
			
			jsonObject.add("Favorite Musics", jsonArray);
			
			gson.toJson(jsonObject, writer);
			
		} catch(Exception e) {}
	}
	
	public void play() {
		
		if(currentMusic == null) {
			return;
		}
		
		if(mediaPlayer != null) {
			mediaPlayer.dispose();
		}
		
		if(media == null || mediaPlayer == null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new JFXPanel();
				}
			});
		}
		
		media = new Media(currentMusic.getAudio().toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAudioSpectrumInterval(0.06);
		mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
			
			@Override
			public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
				
				ComboSetting setting = MusicInfoMod.getInstance().getDesignSetting();
				boolean isWaveform = setting.getOption().getTranslate().equals(TranslateText.WAVEFORM);
				
				for(int i = 0; i < 100; i++) {
					
					if(isWaveform) {
						MusicWaveform.visualizer[i] = (float) ((magnitudes[i] + 60) * (-1.17));
					} else {
						MusicWaveform.visualizer[i] = (float) ((magnitudes[i] + 60) * (-3.0));
					}
				}
			}
		});
		
		mediaPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				stop();
				currentMusic = musics.get(RandomUtils.getRandomInt(0, musics.size() - 1));
				play();
			}
		});
		
		mediaPlayer.play();
		setVolume();
	}
	
	public void setVolume() {
		if(mediaPlayer != null) {
			mediaPlayer.setVolume(GlobalSettingsMod.getInstance().getVolumeSetting().getValue());
		}
	}
	
	public void next() {
		
		if(currentMusic == null) {
			return;
		}
		
		int max = musics.size();
		int index = musics.indexOf(currentMusic);
		
		if(index < max - 1) {
			index++;
		}else {
			index = 0;
		}
		
		currentMusic = musics.get(index);
		play();
	}
	
	public void back() {
		
		if(currentMusic == null) {
			return;
		}
		
		int max = musics.size();
		int index = musics.indexOf(currentMusic);
		
		if(index > 0) {
			index--;
		}else {
			index = max - 1;
		}
		
		currentMusic = musics.get(index);
		play();
	}
	
	public void switchPlayBack() {
		if(mediaPlayer != null) {
			if(mediaPlayer.getStatus().equals(Status.PAUSED)) {
				mediaPlayer.play();
			}else if(mediaPlayer.getStatus().equals(Status.PLAYING)) {
				mediaPlayer.pause();
			}
		}
	}
	
	public void stop() {
		if(mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}
	
	public boolean isPlaying() {
		
		if(mediaPlayer == null) {
			return false;
		}
		
		return mediaPlayer.getStatus().equals(Status.PLAYING);
	}
	
	public float getCurrentTime() {
		
		if(mediaPlayer == null) {
			return 0;
		}
		
		return (float) mediaPlayer.getCurrentTime().toSeconds();
	}
	
	public float getEndTime() {
		
		if(mediaPlayer == null) {
			return 0;
		}
		
		return (float) mediaPlayer.getMedia().getDuration().toSeconds();
	}
	
	public void load() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		File musicDir = fileManager.getMusicDir();
		File cacheDir = new File(fileManager.getCacheDir(), "music");
		
		if(!cacheDir.exists()) {
			fileManager.createDir(cacheDir);
		}
		
		for(File f : musicDir.listFiles()) {
			
			if(FileUtils.getExtension(f).equals("mp3")) {
				
				File imageFile = new File(cacheDir, f.getName().replace(".mp3", ""));
				
				if(!imageFile.exists()) {
					
					try {
						
						Mp3File mp3File = new Mp3File(f);
						
						if(mp3File.hasId3v2Tag()) {
							
							ID3v2 id3v2tag = mp3File.getId3v2Tag();
							
							if(id3v2tag.getAlbumImage() != null) {
								
								byte[] imageData = id3v2tag.getAlbumImage();
								
								FileOutputStream fos = new FileOutputStream(imageFile);
								
								fos.write(imageData);
								fos.close();
								
								ImageIO.write(ImageUtils.resize(ImageIO.read(imageFile), 256, 256), "png", imageFile);
							}
						}
						
					} catch(Exception e) {}
				}
			}
		}
		
		for(File f : musicDir.listFiles()) {
			if(FileUtils.isAudioFile(f)) {
				
				if(getMusicByAudioFile(f) != null) {
					continue;
				}
				
				if(FileUtils.getExtension(f).equals("mp3")) {
					
					File imageFile = new File(cacheDir, f.getName().replace(".mp3", ""));
					
					if(imageFile.exists()) {
						musics.add(new Music(f, imageFile, MusicType.ALL));
					}else {
						musics.add(new Music(f, null, MusicType.ALL));
					}
				}else {
					musics.add(new Music(f, null, MusicType.ALL));
				}
			}
		}
	}
	
	public void loadAsync() {
		Multithreading.runAsync(()-> {
			load();
		});
	}
	
	public Music getMusicByName(String name) {
		
		for(Music m : musics) {
			if(m.getName().equals(name)) {
				return m;
			}
		}
		
		return null;
	}
	
	public Music getMusicByAudioFile(File file) {
		
		for(Music m : musics) {
			if(m.getAudio().equals(file)) {
				return m;
			}
		}
		
		return null;
	}
	
	public void delete(Music m) {
		musics.remove(m);
		m.getAudio().delete();
		load();
	}

	public CopyOnWriteArrayList<Music> getMusics() {
		return musics;
	}

	public Music getCurrentMusic() {
		return currentMusic;
	}

	public void setCurrentMusic(Music currentMusic) {
		this.currentMusic = currentMusic;
	}

	public Ytdlp getYtdlp() {
		return ytdlp;
	}
}
