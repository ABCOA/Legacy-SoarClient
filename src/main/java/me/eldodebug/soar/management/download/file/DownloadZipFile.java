package me.eldodebug.soar.management.download.file;

import java.io.File;

public class DownloadZipFile extends DownloadFile {

	private long unzippedSize;
	
	public DownloadZipFile(String url, String fileName, File outputDir, long size, long unzippedSize) {
		super(url, fileName, outputDir, size);
		this.unzippedSize = unzippedSize;
	}

	public long getUnzippedSize() {
		return unzippedSize;
	}
}
