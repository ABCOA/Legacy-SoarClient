package me.eldodebug.soar.management.download.file;

import java.io.File;

public class DownloadFile {

	private String url;
	private String fileName;
	private File outputDir;
	private long size;
	
	public DownloadFile(String url, String fileName, File outputDir, long size) {
		this.url = url;
		this.fileName = fileName;
		this.outputDir = outputDir;
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public String getFileName() {
		return fileName;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public long getSize() {
		return size;
	}
}
