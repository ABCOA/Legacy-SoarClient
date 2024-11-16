package me.eldodebug.soar.utils.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;

public class FileTransferable  implements Transferable {
	
    private final File file;

    public FileTransferable(File file) {
    	this.file = file;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.javaFileListFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.javaFileListFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        final ArrayList<File> files = new ArrayList<>();
        files.add(file);
        return files;
    }
}
