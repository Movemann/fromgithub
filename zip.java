import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GitHubZipReader {
    public static void main(String[] args) {
        String repoUrl = "https://github.enterprise.com/usuario/repositorio";
        String zipFileName = "archivo.zip";
        String personalAccessToken = "tu_token_personal";

        try {
            // Descargar el archivo ZIP del repositorio de GitHub
            downloadFile(repoUrl + "/archive/main.zip", zipFileName, personalAccessToken);

            // Leer el archivo ZIP descargado
            FileInputStream fis = new FileInputStream(zipFileName);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry.getName());

                // Crear el archivo de destino
                File outputFile = new File(entry.getName());
                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = zis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                bos.close();
                fos.close();
            }

            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadFile(String fileUrl, String fileName, String token) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "token " + token);

            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileName);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
