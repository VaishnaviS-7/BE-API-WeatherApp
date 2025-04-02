import com.sun.net.httpserver.HttpServer;
import handlers.WeatherHandler;

public class Router {
    public static void setupRoutes(HttpServer server) {
        server.createContext("/weather", new WeatherHandler())
.getFilters()
.add(new Server.CorsFilter());
    }
}
