package srdm.cloud.commonService.util.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import srdm.cloud.commonService.util.Config;

@Component
public class MongoLogCtl {

	private static final Logger logger = LoggerFactory.getLogger(MongoLogCtl.class);

	/**
	 * ログファイル 保存期間（月）
	 */
	@Value("${srdm.log.limitPeriodMonth}")
	private long limitPeriodMonth;

	/**
	 * ログファイル 保存数
	 */
	@Value("${srdm.log.limitFileCount}")
	private long limitFileCount;

	/**
	 * MongoDB ログファイル削除処理
	 * @return true:正常 false:エラー
	 */
	public boolean oldLogFileDelete() {
		return oldLogFileDelete(new Config().getMongoLog(), this.limitFileCount, this.limitPeriodMonth);
	}

	/**
	 * MongoDB ログファイル削除処理
	 *
	 * @param path MongoDB ログファイルパス
	 * @param limitFileCount ログファイル 保存数
	 * @param limitPeriodMonth ログファイル 保存期間（月）
	 * @return true:正常 false:エラー
	 */
	public static boolean oldLogFileDelete(String path, long limitFileCount, long limitPeriodMonth) {
		logger.info("oldLogFileDelete start");
		if (path == null) {
			throw new IllegalArgumentException("log file path null");
		}
		logger.info("path:" + path);
		logger.info("limitFileCount:" + limitFileCount + " limitPeriodMonth:" + limitPeriodMonth);
		try {
			// 期間指定のファイル削除を実行する
			if (limitPeriodMonth != 0) {
				deleteLogFileWherePeriod(limitPeriodMonth, getFileList(path));
			}

			// ファイル数指定のファイル削除を実行する。
			if (limitFileCount != 0) {
				deleteLogFileWhereCount(limitFileCount, getFileList(path));
			}

		} catch (IOException | RuntimeException e) {
			logger.error("MongoDB Log Delete Exception:", e);
			return false;
		}
		logger.info("oldLogFileDelete end");
		return true;
	}

	/**
	 * ログファイル判定
	 * @param fileName ログファイル名
	 * @return true:ログファイル false:ログファイルでない
	 */
	public static boolean isLogFile(String fileName) {
		if (fileName == null) {
			return false;
		}
		// 旧フォーマット
		// MongoDbLog--05-16_1816.log
		// MongoDbLog--05-16_1816.log.2018-05-16T09-16-44

		// 新フォーマット
		// SRDMDbLog.log
		// SRDMDbLog.log.2018-05-16T09-16-44
		if (fileName.matches("SRDMDbLog.log")
				|| fileName.matches("MongoDbLog--[0-9]{2}-[0-9]{2}_[0-9]{4}.log")
				|| fileName.matches("SRDMDbLog.log.[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}-[0-9]{2}-[0-9]{2}")
				|| fileName.matches("MongoDbLog--[0-9]{2}-[0-9]{2}_[0-9]{4}.log.[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}-[0-9]{2}-[0-9]{2}")) {
			return true;
		}
		return false;
	}

	/**
	 * 対象フォルダ内のファイル一覧取得（更新日時降順）
	 * @param path 対象フォルダ
	 * @return ファイル一覧
	 * @throws IOException
	 */
	public static Stream<Path> getFileList(String path) throws IOException {
		if (path == null) {
			throw new IllegalArgumentException("log file path null");
		}
		Stream<Path> files = Files.list(Paths.get(path))
				.sorted((o1, o2) -> {
					try {
						return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
					} catch (IOException e1) {
						// 更新日時が存在しない場合はないので考慮不要
						return 0;
					}
				});

		// ログファイル以外のファイルを除外する
		return files.filter(p -> {
			return isLogFile(p.getFileName().toString());
		});
	}

	/**
	 * 対象期間外のファイルを削除する
	 *
	 * @param limitPeriodMonth 保存期間
	 * @param files 検証対象ファイルリスト（ログファイル外なし）
	 * @throws IOException
	 * @throws FileSystemException
	 */
	public static void deleteLogFileWherePeriod(long limitPeriodMonth, Stream<Path> files) throws IOException, FileSystemException {
		deleteLogFileWherePeriod(files, ZonedDateTime.now(ZoneOffset.UTC).minusMonths(limitPeriodMonth));
	}

	/**
	 * 対象期間外のファイルを削除する
	 *
	 * @param files 検証対象ファイルリスト（ログファイル外なし）
	 * @param limitDateTime 基準日時
	 * @throws IOException
	 * @throws FileSystemException
	 */
	public static void deleteLogFileWherePeriod(Stream<Path> files, ZonedDateTime limitDateTime) throws IOException, FileSystemException {
		logger.info("deleteLogFileWherePeriod() stert");
		logger.debug("deleteLogFileWherePeriod() limitDateTime:" + limitDateTime);
		files.filter(f -> {
			try {
				FileTime ft = Files.getLastModifiedTime(f);
				return limitDateTime.toInstant().compareTo(ft.toInstant()) > 0;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		})
		.forEach(p -> {
			try {
				logger.info("Delete Log File Name:" + p.getFileName());
				Files.delete(p);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		logger.info("deleteLogFileWherePeriod() end");
	}

	/**
	 * ファイル上限数以上のファイルを削除する
	 *
	 * @param limitFileCount 保存ファイル数
	 * @param files 検証対象ファイルリスト
	 * @throws IOException
	 * @throws FileSystemException
	 */
	public static void deleteLogFileWhereCount(long limitFileCount, Stream<Path> files) throws IOException, FileSystemException {
		logger.info("deleteLogFileWhereCount() stert");
		Path[] pathList = files.toArray(Path[]::new);
		long delCount = pathList.length - limitFileCount;
		delCount = delCount < 0 ? 0 : delCount;

		Stream.of(pathList).limit(delCount).forEach(p -> {
			try {
				logger.info("Delete Log File Name:" + p.getFileName());
				Files.delete(p);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		logger.info("deleteLogFileWhereCount() end");
	}
}
