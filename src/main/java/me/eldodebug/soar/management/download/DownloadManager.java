package me.eldodebug.soar.management.download;

import java.io.File;
import java.util.ArrayList;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.download.file.DownloadFile;
import me.eldodebug.soar.management.download.file.DownloadZipFile;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.file.FileUtils;
import me.eldodebug.soar.utils.network.HttpUtils;

public class DownloadManager {

	private ArrayList<DownloadFile> downloadFiles = new ArrayList<DownloadFile>();
	
	private boolean downloaded;
	
	public DownloadManager() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		
		downloaded = false;
		
		downloadFiles.add(new DownloadFile("https://files.soarclient.com/ytdlp/ytdlp.exe",
				"ytdlp.exe", new File(fileManager.getExternalDir(), "ytdlp"), 13947126));
		downloadFiles.add(new DownloadZipFile("https://files.soarclient.com/ytdlp/ffmpeg.zip",
				"ffmpeg.zip", new File(fileManager.getExternalDir(), "ffmpeg"), 51986763, 147600195));
		downloadFiles.add(new DownloadZipFile("https://files.soarclient.com/v1/data/cef/windows.zip",
				"cef.zip", new File(fileManager.getExternalDir(), "cef"), 115822583, 265676507));
		
		Multithreading.runAsync(() -> startDownloads());
	}
	
	private void startDownloads() {
		
		for(DownloadFile df : downloadFiles) {
			
			if(!df.getOutputDir().exists()) {
				df.getOutputDir().mkdirs();
			}
			
			if(df instanceof DownloadZipFile) {
				
				DownloadZipFile dzf = (DownloadZipFile) df;
				
				if(FileUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {
					
					File outputFile = new File(dzf.getOutputDir(), dzf.getFileName());
					
					HttpUtils.downloadFile(dzf.getUrl(), outputFile);
					FileUtils.unzip(outputFile, dzf.getOutputDir());
					outputFile.delete();
				}
			} else {
				
				File outputFile = new File(df.getOutputDir(), df.getFileName());
				
				if(outputFile.length() != df.getSize()) {
					HttpUtils.downloadFile(df.getUrl(), outputFile);
				}
			}
		}
		
		checkFiles();
	}
	
	private void checkFiles() {
		
		for(DownloadFile df : downloadFiles) {
			
			if(df instanceof DownloadZipFile) {
				
				DownloadZipFile dzf = (DownloadZipFile) df;
				
				if(FileUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {
					startDownloads();
				}
			} else {
				
				File outputFile = new File(df.getOutputDir(), df.getFileName());
				
				if(outputFile.length() != df.getSize()) {
					startDownloads();
				}
			}
		}
		
		downloaded = true;
	}

	public boolean isDownloaded() {
		return downloaded;
	}
}