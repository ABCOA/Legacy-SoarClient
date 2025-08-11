package me.eldodebug.soar.management.music;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
import me.eldodebug.soar.management.mods.impl.MusicInfoMod;
import me.eldodebug.soar.management.music.openal.OpenALMusicPlayer;
import me.eldodebug.soar.management.music.ytdlp.Ytdlp;
import me.eldodebug.soar.mp3agic.Mp3File;
import me.eldodebug.soar.mp3agic.interfaces.ID3v2;
import me.eldodebug.soar.utils.ImageUtils;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.RandomUtils;
import me.eldodebug.soar.utils.file.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicManager {

	private CopyOnWriteArrayList<Music> musics = new CopyOnWriteArrayList<Music>();
	private Ytdlp ytdlp = new Ytdlp();
	
	private Music currentMusic;
	private OpenALMusicPlayer player = new OpenALMusicPlayer();
	private float trackLengthSec = 0f;

	public MusicManager() {
		load();
		loadData();

		player.setVisualizerListener(mags -> {
			boolean isWaveform = MusicInfoMod.getInstance()
					.getDesignSetting().getOption().getTranslate().equals(TranslateText.WAVEFORM);
			for (int i = 0; i < 100 && i < MusicWaveform.visualizer.length; i++) {
				float v = mags[i]; // 0..1
				MusicWaveform.visualizer[i] = (float) (v * (isWaveform ? 70.0 : 180.0));
			}
		});

		player.setOnFinished(() -> {
			try {
				stop();
				if (!musics.isEmpty()) {
					currentMusic = musics.get(RandomUtils.getRandomInt(0, musics.size() - 1));
					play();
				}
			} catch (Throwable ignored) {}
		});
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
		if (currentMusic == null) return;

		trackLengthSec = 0f;
		if (FileUtils.getExtension(currentMusic.getAudio()).equalsIgnoreCase("mp3")) {
			try {
				Mp3File mp3 = new Mp3File(currentMusic.getAudio());
				trackLengthSec = mp3.getLengthInSeconds();
			} catch (Exception ignored) {}
		}

		try {
			player.stop();
			if (FileUtils.getExtension(currentMusic.getAudio()).equalsIgnoreCase("mp3")) {
				player.playMp3(currentMusic.getAudio());
			} else {
				System.err.println("Only mp3 supported by OpenAL player for now: " + currentMusic.getAudio());
				return;
			}
			setVolume();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setVolume() {
		player.setVolume(GlobalSettingsMod.getInstance().getVolumeSetting().getValue());
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
		player.pauseResume();
	}
	
	public void stop() {
		player.stop();
	}
	
	public boolean isPlaying() {
		return player.isPlaying();
	}
	
	public float getCurrentTime() {
		return player.getCurrentTimeSec();
	}
	
	public float getEndTime() {
		return trackLengthSec > 0 ? trackLengthSec : Math.max(player.getCurrentTimeSec(), 0f);
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
