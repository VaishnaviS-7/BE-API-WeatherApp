import com.sun.net.httpserver.HttpServer;
import handlers.UserHandler;
import handlers.WeatherHandler;

public class Router {
    public static void setupRoutes(HttpServer server) {
        // Use the nested CorsFilter via Server.CorsFilter (no import needed)
        server.createContext("/users", new UserHandler())
              .getFilters()
              .add(new Server.CorsFilter());

        server.createContext("/weather", new WeatherHandler())
              .getFilters()
              .add(new Server.CorsFilter());
    }
}
