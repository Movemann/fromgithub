import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubFileReader {

    public static void main(String[] args) {
        String token = "TU_TOKEN_DE_ACCESO";
        String repoUrl = "https://github.enterprise.com/repositorio";
        String apiUrl = repoUrl + "/contents"; // URL de la API para obtener la lista de archivos

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray filesArray = new JSONArray(response.toString());
                for (int i = 0; i < filesArray.length(); i++) {
                    JSONObject fileObject = filesArray.getJSONObject(i);
                    if (fileObject.getString("type").equals("file")) {
                        String fileUrl = fileObject.getString("download_url");
                        String fileName = fileObject.getString("name");
                        System.out.println("Contenido de " + fileName + ":");
                        readAndPrintFileContent(fileUrl, token);
                    }
                }
            } else {
                System.out.println("Error al obtener la lista de archivos. Código de respuesta: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readAndPrintFileContent(String fileUrl, String token) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + token);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            System.out.println(); // Salto de línea para separar el contenido de los archivos
        } else {
            System.out.println("Error al leer el archivo. Código de respuesta: " + responseCode);
        }
    }
}
