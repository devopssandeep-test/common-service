package srdm.cloud.commonService.util;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

public final class DiskUtil {
	private Logger log;
	private String mTargetPath;

	/**
	 *
	 * @param targetPath 対象パス
	 * @param logger Log4J
	 */
	public DiskUtil(final String targetPath, final Logger logger) {
		log = logger;
		mTargetPath = targetPath;
	}

	public long getFolderSize() {
		long size = 0;
		try {
			File file = new File(mTargetPath);
			for (File f: file.listFiles()) {
				size += calc(f);
			}
		} catch (NullPointerException e) {
			log.error("getFolderSize(): targetPath=[" + mTargetPath + "] Exception: " + e);
		}
		return size;
	}

	private long calc(File file) {
		long size = 0;
		if (file.isDirectory() == true) {
			for (File f: file.listFiles()) {
				size += calc(f);
			}
		} else {
			size = file.length();
		}
		return size;
	}

	public long getAvailableSize() {
		long size = -1;
		try {
			Path root = Paths.get(mTargetPath).toAbsolutePath().getRoot();
			if (root != null) {
				size = root.toFile().getFreeSpace();
			} else {
				log.error("getAvailableSize(): targetPath=[" + mTargetPath + "] Can't get the Root.");
			}
		} catch (InvalidPathException | SecurityException | java.io.IOError e) {
			log.error("getAvailableSize(): targetPath=[" + mTargetPath + "] Exception: " + e);
		}
		return size;
	}
}
