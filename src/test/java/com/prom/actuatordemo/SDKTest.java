package com.prom.actuatordemo;

import com.cbim.dataservice.client.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SDKTest {

    private static final String endpoint = "http://120.52.22.138";
    //private static final String endpoint = "http://localhost:8080";
    private final String storageSpace = "clientsdk-test-ss-004";
    private static final String folderPath = "/a/b/c";
    private final String uniqueSS = "clientsdk-test-ss-" + UUID.randomUUID().toString();
    private final String uniqueFolder = "/" + UUID.randomUUID().toString() + "/a/b/c";
    Optional<String> requestID = Optional.of("requestId");

    // test chunk-size: 4096 bytes
    // fileSize: 18 bytes, chunk-length: 1
    private static final String singleChunkFile = "singlechunk.txt";
    // fileSize: 166095 bytes, chunk-length: 41
    private static final String multiChunkFile = "multichunk.png";
    // fileSize: 291237 bytes, chunk-length: 72
    private static final String bigChunkFile = "bigchunk.tar.gz";
    // 中文文档
    private static final String file_CN_ZH = "DMS中文测试文档.docx";
    private static final char filePathSeparator = File.separatorChar;

    private static final String downloadSingleChunkFile = "singlechunk.txt";
    private static final String downloadMultiChunkFile = "multichunk.png";
    private static final String downloadBigChunkFile = "bigchunk.tar.gz";
    private static final String downloadCNZHFile = "DMS中文测试文档.docx";
    private static final String downloadISSingleChunkFile = "is-singlechunk.txt";
    private static final String downloadISMultiChunkFile = "is-multichunk.png";
    private static final String downloadISBigChunkFile = "is-bigchunk.tar.gz";
    private static final String downloadISCNZHFile = "is-DMS中文测试文档.docx";

    Session session;
    String testResourcePath;

    @BeforeAll
    void beforeAll() {
        session = DmsClient.create("appid-notused", "appkey-notused").createSession("user-notused");
        testResourcePath = Objects.requireNonNull(SDKTest.class.getClassLoader().getResource("")).getPath();
        prepareTempDir();
        cleanUp();
    }

    @AfterAll
    void afterAll() {
        //cleanUp();
    }

    public void prepareTempDir() {
        File file = new File(testResourcePath + "tmp");
        if (! file.exists()) {
            file.mkdirs();
        } else {
            cleanUp();
        }
        // 确保ss和folder有防止upload和download失败
//        try {
//            session.createStorageSpace(storageSpace, requestID);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            session.createFolder(storageSpace, folderPath, requestID);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
    }

    void cleanUp() {
        deleteFile(downloadSingleChunkFile);
        deleteFile(downloadMultiChunkFile);
        deleteFile(downloadBigChunkFile);
        deleteFile(downloadCNZHFile);
        deleteFile(downloadISSingleChunkFile);
        deleteFile(downloadISMultiChunkFile);
        deleteFile(downloadISBigChunkFile);
        deleteFile(downloadISCNZHFile);
    }

    private void deleteFile(String name) {
        File file = new File(testResourcePath + "tmp" + filePathSeparator + name);
        if (file.exists()) {
            file.delete();
        }
    }

    private void deleteFolder(String storage, String folder) {
        for (String sub : subFolderPaths(folder)) {
            final Result<String> result = session.deleteFolder(storage, sub, requestID);
            assertThat(result.getRequestId()).isNotNull();
        }

    }

    @Order(1)
    @Test
    public void createStorageSpace() throws DmsException {
        final Result<String> result = session.createStorageSpace(uniqueSS, requestID);
        assertThat(result.getRequestId()).isNotNull();
    }

    @Order(1)
    @Test
    public void createStorageSpaceExists() {
        try{
            session.createStorageSpace(uniqueSS, requestID);
        } catch (DmsServerException ex) {
            System.out.println(ex.getMessage());
            assertThat(ex.getErrorCode()).isEqualTo("45001");
        }
    }

    @Order(2)
    @Test
    public void createFolder() throws DmsException {
        final Result<String> result = session.createFolder(uniqueSS, uniqueFolder, requestID);
        assertThat(result.getRequestId()).isNotNull();
    }

    @Order(2)
    @Test
    public void createFolderExists() {
        try{
            session.createFolder(uniqueSS, uniqueFolder, requestID);
        } catch (DmsServerException ex) {
            System.out.println(ex.getMessage());
            assertThat(ex.getErrorCode()).isEqualTo("45201");
        }
    }

    @Order(98)
    @Test
    public void deleteFolder() throws DmsException {
        deleteFolder(uniqueSS, uniqueFolder);
    }


    @Order(98)
    @Test
    public void deleteFolderNotExists() {
        try{
            session.deleteFolder(uniqueSS, "/path/of/folder/no/exists", requestID);
        } catch (DmsServerException ex) {
            assertThat(ex.getErrorCode()).isEqualTo("45200");
        }
    }

    @Order(99)
    @Test
    public void deleteStorageSpace() throws DmsException {
        final Result<String> result = session.deleteStorageSpace(uniqueSS, requestID);
        assertThat(result.getRequestId()).isNotNull();
    }

    @Order(99)
    @Test
    public void deleteStorageSpaceNotExists() {
        try{
            session.deleteStorageSpace( "storage-space-not-exist", requestID);
        } catch (DmsServerException ex) {
            System.out.println(ex.getMessage());
            assertThat(ex.getErrorCode()).isEqualTo("45000");
        }
    }

    @Order(3)
    @Test
    public void testUploadSingleChunk() throws DmsException {
        final String localFullPath =  testResourcePath + singleChunkFile;
        final String serverFullPath = folderPath + "/" + singleChunkFile;

        final Result<Map<Integer, String>> result = session.uploadFile(localFullPath, storageSpace, serverFullPath, null,
                true, null, null, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d, ETag:%s", entry.getKey(), entry.getValue()));
        }

        final File file = new File(localFullPath);
        System.out.println(String.format("TotalChunkLength:%d, fileSize:%d bytes", result.getData().size(), file.length()));
        assertThat(result.getData().size()).isEqualTo(1);
    }

    @Order(4)
    @Test
    public void testUploadMultiChunk() throws DmsException {
        final String localFullPath =  testResourcePath + multiChunkFile;
        final String serverFullPath = folderPath + "/" + multiChunkFile;

        final Result<Map<Integer, String>> result = session.uploadFile(localFullPath, storageSpace, serverFullPath, null,
                true, null, null, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d, ETag:%s", entry.getKey(), entry.getValue()));
        }

        File file = new File(localFullPath);
        System.out.println(String.format("TotalChunkLength:%d, fileSize:%d bytes", result.getData().size(), file.length()));
        assertThat(result.getData().size()).isGreaterThan(0);
    }

    @Order(5)
    @Test
    public void testUploadHugeChunk() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + bigChunkFile;
        final String serverFullPath = folderPath + "/" + bigChunkFile;

        final Result<Map<Integer, String>> result = session.uploadFile(localFullPath, storageSpace, serverFullPath, null,
                true, null, null, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d, ETag:%s", entry.getKey(), entry.getValue()));
        }

        File file = new File(localFullPath);
        System.out.println(String.format("TotalChunkLength:%d, fileSize:%d bytes", result.getData().size(), file.length()));
        assertThat(result.getData().size()).isGreaterThan(0);
    }

    @Order(6)
    @Test
    public void downloadSingleChunkFile() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadSingleChunkFile;
        final String serverFullPath = folderPath + "/" + singleChunkFile;

        final Result<Map<Integer, String>> result = session.download(storageSpace, serverFullPath, localFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d  ETag:%s", entry.getKey(), entry.getValue()));
        }

        final File srcFile = new File(testResourcePath + singleChunkFile);
        final File downloadFile = new File(localFullPath);
        System.out.println(String.format("fileSize:%d", downloadFile.length()));
        assertThat(srcFile.length()).isEqualTo(downloadFile.length());
    }

    @Order(7)
    @Test
    public void downloadMultiChunkFile() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadMultiChunkFile;
        final String serverFullPath = folderPath + "/" + multiChunkFile;

        final Result<Map<Integer, String>> result = session.download(storageSpace, serverFullPath, localFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d  ETag:%s", entry.getKey(), entry.getValue()));
        }

        final File srcFile = new File(testResourcePath + multiChunkFile);
        final File downloadFile = new File(localFullPath);
        System.out.println(String.format("fileSize:%d", downloadFile.length()));
        assertThat(srcFile.length()).isEqualTo(downloadFile.length());
    }

    @Order(8)
    @Test
    public void downloadHugeChunkFile() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadBigChunkFile;
        final String serverFullPath = folderPath + "/" + bigChunkFile;

        final Result<Map<Integer, String>> result = session.download(storageSpace, serverFullPath, localFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d  ETag:%s", entry.getKey(), entry.getValue()));
        }

        final File srcFile = new File(testResourcePath + bigChunkFile);
        final File downloadFile = new File(localFullPath);
        System.out.println(String.format("fileSize:%d", downloadFile.length()));
        assertThat(srcFile.length()).isEqualTo(downloadFile.length());
    }

    @Order(9)
    @Test
    public void downloadSingleChunkInputStream() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadISSingleChunkFile;
        final String serverFullPath = folderPath + "/" + singleChunkFile;

        final Path downloadPath = Paths.get(localFullPath);
        final DownloadInputStreamResult result = session.downloadInputStream(storageSpace, serverFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());
        try (SequenceInputStream is = result.getSequenceInputStream();
             OutputStream os = Files.newOutputStream(downloadPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            final byte[] buf = new byte[8 * 1024];
            int off;
            while ((off = is.read(buf)) != -1) {
                os.write(buf,0, off);
                os.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final File srcFile = new File(testResourcePath + singleChunkFile);
        final File newDownloadFile = new File(localFullPath);
        assertThat(srcFile.length()).isEqualTo(newDownloadFile.length());
    }

    @Order(10)
    @Test
    public void downloadMultiChunkInputStream() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadISMultiChunkFile;
        final String serverFullPath = folderPath + "/" + multiChunkFile;

        final Path downloadPath = Paths.get(localFullPath);
        final DownloadInputStreamResult result = session.downloadInputStream(storageSpace, serverFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());
        try (SequenceInputStream is = result.getSequenceInputStream();
             OutputStream os = Files.newOutputStream(downloadPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            final byte[] buf = new byte[8 * 1024];
            int off;
            while ((off = is.read(buf)) != -1) {
                os.write(buf,0, off);
                os.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final File srcFile = new File(testResourcePath + multiChunkFile);
        final File newDownloadFile = new File(localFullPath);
        assertThat(srcFile.length()).isEqualTo(newDownloadFile.length());
    }

    @Order(11)
    @Test
    public void downloadHugeChunkInputStream() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadISBigChunkFile;
        final String serverFullPath = folderPath + "/" + bigChunkFile;

        final Path downloadPath = Paths.get(localFullPath);
        final DownloadInputStreamResult result = session.downloadInputStream(storageSpace, serverFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());
        try (SequenceInputStream is = result.getSequenceInputStream();
             OutputStream os = Files.newOutputStream(downloadPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            final byte[] buf = new byte[8 * 1024];
            int off;
            while ((off = is.read(buf)) != -1) {
                os.write(buf,0, off);
                os.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final File srcFile = new File(testResourcePath + bigChunkFile);
        final File newDownloadFile = new File(localFullPath);
        assertThat(srcFile.length()).isEqualTo(newDownloadFile.length());
    }

    @Order(12)
    @Test
    public void uploadCNZHFile() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + file_CN_ZH;
        final String serverFullPath = folderPath + "/" + file_CN_ZH;

        final Result<Map<Integer, String>> result = session.uploadFile(localFullPath, storageSpace, serverFullPath, null,
                true, null, null, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d, ETag:%s", entry.getKey(), entry.getValue()));
        }

        final File file = new File(localFullPath);
        System.out.println(String.format("TotalChunkLength:%d, fileSize:%d bytes", result.getData().size(), file.length()));
        assertThat(result.getData().size()).isGreaterThan(0);
    }

    @Order(13)
    @Test
    public void downloadCNZHFile() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadCNZHFile;
        final String serverFullPath = folderPath + "/" + file_CN_ZH;

        final Result<Map<Integer, String>> result = session.download(storageSpace, serverFullPath, localFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());

        for (Map.Entry<Integer, String> entry : result.getData().entrySet()) {
            System.out.println(String.format("seqNum:%d  ETag:%s", entry.getKey(), entry.getValue()));
        }

        final File srcFile = new File(testResourcePath + file_CN_ZH);
        final File downloadFile = new File(localFullPath);
        System.out.println(String.format("fileSize:%d", downloadFile.length()));
        assertThat(srcFile.length()).isEqualTo(downloadFile.length());
    }

    @Order(13)
    @Test
    public void downloadCNZHFileInputStream() throws DmsException, ExecutionException, InterruptedException {
        final String localFullPath =  testResourcePath + "tmp" + filePathSeparator + downloadISCNZHFile;
        final String serverFullPath = folderPath + "/" + file_CN_ZH;

        final Path downloadPath = Paths.get(localFullPath);
        final DownloadInputStreamResult result = session.downloadInputStream(storageSpace, serverFullPath, requestID);
        assertThat("success").isEqualTo(result.getCode());
        try (SequenceInputStream is = result.getSequenceInputStream();
             OutputStream os = Files.newOutputStream(downloadPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            final byte[] buf = new byte[8 * 1024];
            int off;
            while ((off = is.read(buf)) != -1) {
                os.write(buf,0, off);
                os.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final File srcFile = new File(testResourcePath + file_CN_ZH);
        final File newDownloadFile = new File(localFullPath);
        assertThat(srcFile.length()).isEqualTo(newDownloadFile.length());
    }

    @Test
    public void getTestResourcePath() {
        System.out.println(SDKTest.class.getResource("/"));
        System.out.println(SDKTest.class.getClassLoader().getResource(""));
        System.out.println(File.separatorChar);
    }

    public static List<String> subFolderPaths(String path) {
        List<String> paths = new ArrayList<>();
        String currentPath = "";
        for (String sub : path.split("/")) {
            if (sub.isEmpty()) continue;    //root dir is not allowed to delete (a good test case)
            currentPath = currentPath + "/" + sub;
            paths.add(currentPath);
        }
        paths.sort((o1, o2) -> o2.length() - o1.length());
        return paths;
    }
}
