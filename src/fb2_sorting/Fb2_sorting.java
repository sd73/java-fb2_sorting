/*
 * Скрипт каталогизирует по хуш-сумме fb2 файлы
 * ============================================
 * 1 шаг - просчитывается md5 файлы
 * 2 шаг - файл переносится в каталог хеш[1]/хеш[2]/хеш[3]
 * 3 шаг - перенесенный файл переименовывается в "md5".fb2
 * 4 шаг - файл "md5".fb2 архивируется в "md5".zip
 * 5 шаг - исходный файл удаляется
 */

package fb2_sorting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author demidov
 */
public class Fb2_sorting {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String inputpath = "";
        if (args.length==0) {
            inputpath = "/home/demidov/temp/fb2";
            // inputpath = "d:/demidov/temp/fb2";
        } else {
            inputpath = args[0];
        }
        String ext = ".fb2";
        fb2Sorting(inputpath, ext);
    }

    private static void fb2Sorting(String dir, String ext) throws IOException, NoSuchAlgorithmException {
        File file = new File(dir);
        if (!file.exists()) {
            System.out.println(dir + " папка не существует");
        }
        File[] listFiles = file.listFiles(new MyFileNameFilter(ext));
        if (listFiles.length == 0) {
            System.out.println(dir + " не содержит файлов с расширением " + ext);
        } else {
            System.out.println("В '" + dir + "' найдено файлов с расширением " + ext + ": " + listFiles.length);
            // количество найденных файлов
            Integer ContFB2 = listFiles.length;
            // счётчик обработки файлов
            Integer Counter = 1;
            for (File f : listFiles) {
                System.out.print("Обрабатывается " + Counter + " из " + ContFB2 + ": " + f.getName() + " - ");
                // получаем md5 сумму обрабатываемого файла
                MessageDigest md = MessageDigest.getInstance("MD5");
                String hex = checksum(dir + File.separator + f.getName(), md);
                char[] dst1 = new char[2];
                char[] dst2 = new char[2];
                char[] dst3 = new char[2];
                hex.getChars(0, 2, dst1, 0); //
                hex.getChars(2, 4, dst2, 0); // 
                hex.getChars(4, 6, dst3, 0); // 
                // создаем каталог с подкаталогами  для файла в ...out/MD5[1]/MD5[2]/MD5[3]
                new File(dir + File.separator + "out" + File.separator + String.valueOf(dst1) + File.separator + String.valueOf(dst2) + File.separator + String.valueOf(dst3)).mkdirs();
                final File originalFile = new File(dir, f.getName());
                //final File newFile = new File(dir + File.separator + "out" + File.separator + String.valueOf(dst1) + File.separator + String.valueOf(dst2) + File.separator + String.valueOf(dst3), f.getName());
                final File newFile = new File(dir + File.separator + "out" + File.separator + String.valueOf(dst1) + File.separator + String.valueOf(dst2) + File.separator + String.valueOf(dst3), hex+ext);
                if (originalFile.renameTo(newFile)) {
                    System.out.print("Koпиpoвaниe пpoшлo ycпeшнo.");
                } else {
                    System.out.print("Koпиpoвaниe нe yдaлocь.");
                }
                
                FileOutputStream fos = new FileOutputStream(dir + File.separator + "out" + File.separator + String.valueOf(dst1) + File.separator + String.valueOf(dst2) + File.separator + String.valueOf(dst3) + File.separator + hex + ".zip");
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                File fileToZip = new File(dir + File.separator + "out" + File.separator + String.valueOf(dst1) + File.separator + String.valueOf(dst2) + File.separator + String.valueOf(dst3), hex+ext);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.close();
                System.out.print(" Сжато.");
                fis.close();
                fos.close();
                fileToZip.delete();
                System.out.print(" Исходный файл удален.");
                System.out.println("");
                Counter++;
            }
        }
    }

    private static String checksum(String filepath, MessageDigest md) throws IOException {
        try (InputStream fis = new FileInputStream(filepath)) {
            byte[] buffer = new byte[1024];
            int nread;
            while ((nread = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nread);
            }
        }
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Реализация интерфейса FileNameFilter
    public static class MyFileNameFilter implements FilenameFilter {

        private String ext;

        public MyFileNameFilter(String ext) {
            this.ext = ext.toLowerCase();
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(ext);
        }
    }
}
