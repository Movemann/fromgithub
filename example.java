import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GitHubFileReader {

    public static List<String> leerArchivos(String repoOwner, String repoName, String githubToken) {
        List<String> valores = new ArrayList<>();
        try {
            String apiUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/contents";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + githubToken);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parsear la respuesta JSON para obtener los nombres de los archivos
            String jsonResponse = response.toString();
            String[] fileNames = jsonResponse.split("\"name\":\"");
            for (int i = 1; i < fileNames.length; i++) {
                String fileName = fileNames[i].split("\",")[0];
                String fileContent = obtenerContenidoArchivo(repoOwner, repoName, fileName, githubToken);
                valores.add(fileContent);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return valores;
    }

    private static String obtenerContenidoArchivo(String repoOwner, String repoName, String fileName, String githubToken) throws IOException {
        String apiUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/contents/" + fileName;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + githubToken);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parsear la respuesta JSON para obtener el contenido del archivo
        String jsonResponse = response.toString();
        String fileContent = jsonResponse.split("\"content\":\"")[1].split("\",")[0];
        return new String(java.util.Base64.getDecoder().decode(fileContent));
    }

    public static void main(String[] args) {
        String repoOwner = "owner"; // Reemplaza "owner" con el nombre del propietario del repositorio
        String repoName = "repositorio"; // Reemplaza "repositorio" con el nombre del repositorio
        String githubToken = "tu_token"; // Reemplaza "tu_token" con tu token de acceso personal de GitHub

        List<String> valores = leerArchivos(repoOwner, repoName, githubToken);

        // Aquí puedes procesar los valores obtenidos como desees
        for (String valor : valores) {
            System.out.println("Valor del archivo: " + valor);
        }
    }
}
