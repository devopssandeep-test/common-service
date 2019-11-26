/**
 *
 */
package srdm.cloud.commonService.util.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import srdm.cloud.commonService.util.Config;

/**
 * @author SBC Y.Haruyama
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class MongoLogCtlTest {

	public static StringWriter writer;
	public static WriterAppender appender;
	private static Logger log = Logger.getLogger(MongoLogCtlTest.class);

	private static String testFolder = "c:/JUnitTest/srdm/cloud/commonService/util/file/MongoLogCtlTest";

	@Rule
	public TestName name = new TestName();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		writer = new StringWriter();
		appender = new WriterAppender(new PatternLayout("%d %5p [%t] %c{1} - %m%n"),writer);
		LogManager.getRootLogger().addAppender(appender);
		LogManager.getRootLogger().setAdditivity(false);

		// テスト用フォルダの作成
		recursiveDeleteFile(new File(testFolder));
		Files.createDirectories(Paths.get(testFolder));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		LogManager.getRootLogger().removeAppender(appender);
		LogManager.getRootLogger().setAdditivity(true);
	}

	/**
	 * 対象のファイルオブジェクトの削除を行う.<BR>
	 * ディレクトリの場合は再帰処理を行い、削除する。
	 *
	 * @param file ファイルオブジェクト
	 * @throws Exception
	 */
	private static void recursiveDeleteFile(final File file) throws Exception {
		// 存在しない場合は処理終了
		if (!file.exists()) {
			return;
		}
		// 対象がディレクトリの場合は再帰処理
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				recursiveDeleteFile(child);
			}
		}
		// 対象がファイルもしくは配下が空のディレクトリの場合は削除する
		file.delete();
	}

	/**
	 * ログファイル作成メソッド
	 *
	 * @param basePath ディレクトリ
	 * @param fileName ファイル名
	 * @param year 更新日時(年)
	 * @param month 更新日時(月)
	 * @param dayOfMonth 更新日時(日)
	 * @throws IOException
	 */
	private void makeFile(String basePath, String fileName, int year, int month, int dayOfMonth) throws IOException {
		makeFile(basePath, fileName, year, month, dayOfMonth, 0, 0, 0, 0);
	}

	/**
	 * ログファイル作成メソッド
	 *
	 * @param basePath ディレクトリ
	 * @param fileName ファイル名
	 * @param year 更新日時(年)
	 * @param month 更新日時(月)
	 * @param dayOfMonth 更新日時(日)
	 * @param hour 更新日時(時）
	 * @param minute 更新日時(分）
	 * @param second 更新日時(秒）
	 * @param nanoOfSecond 更新日時(ミリ秒）
	 * @throws IOException
	 */
	private void makeFile(String basePath, String fileName, int year, int month, int dayOfMonth, int hour, int minute,
			int second, int nanoOfSecond) throws IOException {
		if (Files.exists(Paths.get(basePath + "/" + fileName))) {
			Files.delete(Paths.get(basePath + "/" + fileName));
		}
		Path p = Files.createFile(Paths.get(basePath + "/" + fileName));
		ZonedDateTime z = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, ZoneId.of("Etc/GMT-9"));
		p.toFile().setLastModified(z.toInstant().toEpochMilli() + z.get(ChronoField.NANO_OF_SECOND));
	}

	/**
	 * 正常 ファイルパターン
	 */
	@Test
	public void test_isLogFile001() {
		assertTrue(MongoLogCtl.isLogFile("SRDMDbLog.log"));
		assertTrue(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log"));
		assertTrue(MongoLogCtl.isLogFile("SRDMDbLog.log.2018-05-16T09-16-44"));
		assertTrue(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log.2018-05-16T09-16-44"));
	}

	/**
	 * エラー ファイルパターン酷似
	 */
	@Test
	public void test_isLogFile002() {
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog1.log"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--051-16_0000.log"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--51-160_0000.log"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--51-16_00000.log"));
		assertFalse(MongoLogCtl.isLogFile("SRDMDbLog2.log.20181-05-16T09-16-44"));
		assertFalse(MongoLogCtl.isLogFile("SRDMDbLog.log.20181-05-16T09-16-44"));
		assertFalse(MongoLogCtl.isLogFile("SRDMDbLog.log.2018-025-16T09-16-44"));
		assertFalse(MongoLogCtl.isLogFile("SRDMDbLog.log.2018-05-136T09-16-44"));
		assertFalse(MongoLogCtl.isLogFile("SRDMDbLog.log.2018-05-16T093-16-44"));
		assertFalse(MongoLogCtl.isLogFile("SRDMDbLog.log.2018-05-16T09-156-44"));
		assertFalse(MongoLogCtl.isLogFile("SRDMDbLog.log.2018-05-16T09-16-447"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log.20181-05-16T09-16-44"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log.2018-025-16T09-16-44"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log.2018-05-136T09-16-44"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log.2018-05-16T093-16-44"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log.2018-05-16T09-156-44"));
		assertFalse(MongoLogCtl.isLogFile("MongoDbLog--05-16_0000.log.2018-05-16T09-16-447"));
	}

	/**
	 * 意地悪データ
	 */
	@Test
	public void test_isLogFile003() {
		assertFalse(MongoLogCtl.isLogFile("<>&\"'")); // HTMLエスケープ
		assertFalse(MongoLogCtl.isLogFile("'\"%_％＿\\")); // SQLエスケープ
		assertFalse(MongoLogCtl.isLogFile("")); // 空文字
		assertFalse(MongoLogCtl.isLogFile(" ")); // 半角文字
		assertFalse(MongoLogCtl.isLogFile("　")); // 全角文字
		assertFalse(MongoLogCtl.isLogFile(null)); // null
		assertFalse(MongoLogCtl.isLogFile("あいうえお")); // ひらがな
		assertFalse(MongoLogCtl.isLogFile("¢£¬‖−〜―")); // 文字エンコードによってUnicodeとのマッピングに差がある文字
	}

	/**
	 * 正常 ファイルパターン1 更新日付（日）違い
	 * @throws IOException
	 */
	@Test
	public void test_getFileList001() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 15);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 5, 16);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17);

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}

	/**
	 * 正常 ファイルパターン1 更新日付（月）違い
	 * @throws IOException
	 */
	@Test
	public void test_getFileList002() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 3, 17);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 4, 17);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17);

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}

	/**
	 * 正常 ファイルパターン1 更新日付（年）違い
	 * @throws IOException
	 */
	@Test
	public void test_getFileList003() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2016, 5, 17);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2017, 5, 17);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17);

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}

	/**
	 * 正常 ファイルパターン1 更新日付（秒）違い
	 * @throws IOException
	 */
	@Test
	public void test_getFileList004() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 13, 56, 57, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 5, 17, 13, 56, 58, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 13, 56, 59, 333);

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}

	/**
	 * 正常 ファイルパターン1 更新日付（分）違い
	 * @throws IOException
	 */
	@Test
	public void test_getFileList005() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 13, 57, 59, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 5, 17, 13, 58, 59, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 13, 59, 59, 333);

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}

	/**
	 * 正常 ファイルパターン1 更新日付（時）違い
	 * @throws IOException
	 */
	@Test
	public void test_getFileList006() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 9, 59, 59, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 5, 17, 10, 59, 59, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 11, 59, 59, 333);

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}


	/**
	 * 正常 ログファイルのみ抽出
	 * @throws IOException
	 */
	@Test
	public void test_getFileList007() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "SRDMDbLog.log", 2018, 5, 17, 10, 59, 59, 0);
		makeFile(basePath, "SRDMDbLog.log.2017-05-16T09-16-44", 2017, 5, 16, 9, 16, 44, 0);
		makeFile(basePath, "SRDMDbLog.log.2018-03-31T10-10-00", 2018, 3, 31, 10, 10, 0, 0);

		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2017, 3, 17, 13, 16, 5, 0);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2016-05-16T20-01-25", 2016, 5, 16, 20, 1, 25, 0);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2017-02-16T13-16-44", 2017, 2, 16, 13, 16, 44, 0);

		makeFile(basePath, "MongoDbLog--10-11_0000.log", 2015, 6, 1, 1, 1, 41, 0);
		makeFile(basePath, "MongoDbLog--10-11_0000.log.2013-10-16T09-52-01", 2013, 5, 16, 9, 52, 1, 0);
		makeFile(basePath, "MongoDbLog--10-11_0000.log.2015-04-16T18-44-35", 2015, 5, 17, 18, 44, 35, 0);


		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 9);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--10-11_0000.log.2013-10-16T09-52-01");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--10-11_0000.log.2015-04-16T18-44-35");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--10-11_0000.log");
		assertEquals(pathList[3].getFileName().toString(), "MongoDbLog--05-16_0000.log.2016-05-16T20-01-25");
		assertEquals(pathList[4].getFileName().toString(), "MongoDbLog--05-16_0000.log.2017-02-16T13-16-44");
		assertEquals(pathList[5].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[6].getFileName().toString(), "SRDMDbLog.log.2017-05-16T09-16-44");
		assertEquals(pathList[7].getFileName().toString(), "SRDMDbLog.log.2018-03-31T10-10-00");
		assertEquals(pathList[8].getFileName().toString(), "SRDMDbLog.log");
	}

	/**
	 * 正常 ログファイルのみ抽出
	 * @throws IOException
	 */
	@Test
	public void test_getFileList008() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 9, 59, 7, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log333", 2018, 5, 17, 9, 59, 6, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 9, 59, 5, 333);

		makeFile(basePath, "dfkljalkjf", 2018, 5, 17, 9, 59, 4, 333);

		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 3, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.20dd18-05-16T09-16-44", 2018, 5, 17, 9, 59, 2, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018333-05-16T09-16-44", 2018, 5, 17, 9, 59, 1, 333);

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-15_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-17_0000.log");

	}

	/**
	 * 正常パターン 3ファイル中 1ファイル削除
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWhereCount001() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 9, 59, 1, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 2, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 9, 59, 3, 333);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));
		MongoLogCtl.deleteLogFileWhereCount(2, MongoLogCtl.getFileList(basePath));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 2);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}

	/**
	 * 正常パターン 3ファイル中 2ファイル削除
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWhereCount002() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 9, 59, 1, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 2, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 9, 59, 3, 333);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));
		MongoLogCtl.deleteLogFileWhereCount(1, MongoLogCtl.getFileList(basePath));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 1);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-15_0000.log");
	}

	/**
	 * 正常パターン 3ファイル中 3ファイル削除
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWhereCount003() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 9, 59, 1, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 2, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 9, 59, 3, 333);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));
		MongoLogCtl.deleteLogFileWhereCount(0, MongoLogCtl.getFileList(basePath));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 0);
	}

	/**
	 * 正常パターン 3ファイル中 0ファイル削除
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWhereCount004() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 9, 59, 1, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 2, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 9, 59, 3, 333);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));
		MongoLogCtl.deleteLogFileWhereCount(3, MongoLogCtl.getFileList(basePath));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");

	}

	/**
	 * 正常パターン ログファイル数以上のリミット設定
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWhereCount005() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17, 9, 59, 1, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 2, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 9, 59, 3, 333);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));
		MongoLogCtl.deleteLogFileWhereCount(4, MongoLogCtl.getFileList(basePath));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-15_0000.log");

	}

	/**
	 * 正常パターン ログファイル無し
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWhereCount006() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		MongoLogCtl.deleteLogFileWhereCount(50, MongoLogCtl.getFileList(basePath));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);
		assertEquals(pathList.length, 0);
	}

	/**
	 * 正常パターン 新旧フォーマット混在
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWhereCount007() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "SRDMDbLog.log", 2018, 5, 17, 9, 59, 3, 333);
		makeFile(basePath, "SRDMDbLog.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 2, 333);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2018-05-16T09-16-44", 2018, 5, 17, 9, 59, 1, 333);
		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 5, 17, 9, 59, 0, 333);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));
		MongoLogCtl.deleteLogFileWhereCount(2, MongoLogCtl.getFileList(basePath));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 2);
		assertEquals(pathList[0].getFileName().toString(), "SRDMDbLog.log.2018-05-16T09-16-44");
		assertEquals(pathList[1].getFileName().toString(), "SRDMDbLog.log");
	}

	/**
	 * 正常パターン 1ヶ月比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod001() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 3, 17);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 4, 17);
		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 1;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 2);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-17_0000.log");
	}

	/**
	 * 正常パターン 2ヶ月比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod002() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 3, 17);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 4, 17);
		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 2;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-15_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-17_0000.log");
	}

	/**
	 * 正常パターン 0ヶ月比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod003() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 3, 17);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 4, 17);
		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 0;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 1);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0000.log");
	}

	/**
	 * 正常パターン -1ヶ月比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod004() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-15_0000.log", 2018, 3, 17);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 4, 17);
		makeFile(basePath, "MongoDbLog--05-17_0000.log", 2018, 5, 17);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = -1;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 0);
	}

	/**
	 * 正常パターン 12ヶ月比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod005() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2017, 5, 16);
		makeFile(basePath, "MongoDbLog--05-17_0001.log", 2017, 5, 17);
		makeFile(basePath, "MongoDbLog--05-18_0000.log", 2017, 5, 18);
		makeFile(basePath, "MongoDbLog--05-17_0002.log", 2018, 5, 17);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 12;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0001.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-18_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-17_0002.log");
	}

	/**
	 * 正常パターン 13ヶ月比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod006() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2017, 4, 16);
		makeFile(basePath, "MongoDbLog--05-17_0001.log", 2017, 4, 17);
		makeFile(basePath, "MongoDbLog--05-18_0000.log", 2017, 4, 18);
		makeFile(basePath, "MongoDbLog--05-17_0002.log", 2018, 5, 17);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 13;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-17_0001.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-18_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-17_0002.log");
	}

	/**
	 * 正常パターン 基準日が「うるう年」+ 1ヶ月
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod007() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--01-28_0000.log", 2016, 1, 28);
		makeFile(basePath, "MongoDbLog--01-29_0000.log", 2016, 1, 29);
		makeFile(basePath, "MongoDbLog--01-30_0000.log", 2016, 1, 30);
		makeFile(basePath, "MongoDbLog--01-31_0000.log", 2016, 1, 31);
		makeFile(basePath, "MongoDbLog--02-29_0000.log", 2016, 2, 29);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2016, 2, 29, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 1;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 4);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--01-29_0000.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--01-30_0000.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--01-31_0000.log");
		assertEquals(pathList[3].getFileName().toString(), "MongoDbLog--02-29_0000.log");
	}

	/**
	 * 正常パターン 基準日が「うるう年」+ 12ヶ月
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod008() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--02-27_2015.log", 2015, 2, 27);
		makeFile(basePath, "MongoDbLog--02-28_2015.log", 2015, 2, 28);
		makeFile(basePath, "MongoDbLog--03-01_2015.log", 2015, 3, 1);
		makeFile(basePath, "MongoDbLog--02-29_2016.log", 2016, 2, 29);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2016, 2, 29, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 12;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 3);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--02-28_2015.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--03-01_2015.log");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--02-29_2016.log");
	}

	/**
	 * 正常パターン 削除対象ファイル「うるう年」+ 1ヶ月
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod009() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--02-27_2016.log", 2016, 2, 27);
		makeFile(basePath, "MongoDbLog--02-28_2016.log", 2016, 2, 28);
		makeFile(basePath, "MongoDbLog--02-29_2016.log", 2016, 2, 29);
		makeFile(basePath, "MongoDbLog--03-01_2016.log", 2016, 3, 01);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2016, 3, 29, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 1;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 2);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--02-29_2016.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--03-01_2016.log");
	}

	/**
	 * 正常パターン 削除対象ファイル「うるう年」+ 12ヶ月
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod010() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--02-27_2016.log", 2016, 2, 27);
		makeFile(basePath, "MongoDbLog--02-28_2016.log", 2016, 2, 28);
		makeFile(basePath, "MongoDbLog--02-29_2016.log", 2016, 2, 29);
		makeFile(basePath, "MongoDbLog--03-01_2016.log", 2016, 3, 1);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2017, 3, 1, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 12;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 1);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--03-01_2016.log");
	}

	/**
	 * 正常パターン 秒比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod011() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--04-17_0001.log", 2018, 4, 17, 10, 11, 11, 0);
		makeFile(basePath, "MongoDbLog--04-17_0002.log", 2018, 4, 17, 10, 11, 12, 0);
		makeFile(basePath, "MongoDbLog--04-17_0003.log", 2018, 4, 17, 10, 11, 13, 0);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 10, 11, 12, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 1;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 2);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--04-17_0002.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--04-17_0003.log");
	}

	/**
	 * 正常パターン ミリ秒比較
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod012() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "MongoDbLog--04-17_0001.log", 2018, 4, 17, 10, 11, 12, 0);
		makeFile(basePath, "MongoDbLog--04-17_0002.log", 2018, 4, 17, 10, 11, 12, 1);
		makeFile(basePath, "MongoDbLog--04-17_0003.log", 2018, 4, 17, 10, 11, 12, 2);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 10, 11, 12, 1, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 1;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 2);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--04-17_0002.log");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--04-17_0003.log");
	}

	/**
	 * 正常パターン 新旧混在
	 * @throws IOException
	 */
	@Test
	public void test_deleteLogFileWherePeriod013() throws IOException {
		// 準備
		String basePath = testFolder + "/" + name.getMethodName();
		Files.createDirectories(Paths.get(basePath));

		makeFile(basePath, "SRDMDbLog.log", 2018, 5, 17, 17, 22, 23, 1);
		makeFile(basePath, "MongoDbLog--05-16_0000.log", 2018, 5, 16, 10, 10, 10, 1);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2017-02-01T01-01-01", 2017, 2, 1, 2, 1, 1, 0);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2017-05-16T13-31-44", 2017, 5, 16, 1, 4, 44, 1);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2017-05-17T13-31-44", 2017, 5, 17, 13, 31, 44, 1);
		makeFile(basePath, "MongoDbLog--05-16_0000.log.2017-05-18T01-01-06", 2017, 5, 18, 1, 1, 6, 1);

		MongoLogCtl.getFileList(basePath).forEach(a -> log.debug("fileName:" + a.getFileName().toString()));

		ZonedDateTime z = ZonedDateTime.of(2018, 5, 17, 0, 0, 0, 0, ZoneId.of("Etc/GMT-9"));
		long limitPeriodMonth = 12;
		MongoLogCtl.deleteLogFileWherePeriod(MongoLogCtl.getFileList(basePath), z.minusMonths(limitPeriodMonth));

		Path[] pathList = MongoLogCtl.getFileList(basePath).toArray(Path[]::new);

		assertEquals(pathList.length, 4);
		assertEquals(pathList[0].getFileName().toString(), "MongoDbLog--05-16_0000.log.2017-05-17T13-31-44");
		assertEquals(pathList[1].getFileName().toString(), "MongoDbLog--05-16_0000.log.2017-05-18T01-01-06");
		assertEquals(pathList[2].getFileName().toString(), "MongoDbLog--05-16_0000.log");
		assertEquals(pathList[3].getFileName().toString(), "SRDMDbLog.log");

	}

}
