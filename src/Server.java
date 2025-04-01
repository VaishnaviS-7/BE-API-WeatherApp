import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void start(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            Router.setupRoutes(server);

            // Add CORS filter to the fallback context
            server.createContext("/", exchange -> {
                exchange.sendResponseHeaders(404, -1); // fallback 404 handler
            }).getFilters().add(new CorsFilter());

            server.setExecutor(null); // use default executor
            System.out.println("Server started at http://localhost:" + port);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    // Nested CORS Filter
    static class CorsFilter extends Filter {
        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // Handle preflight OPTIONS requests immediately
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1); // No Content
                return;
            }

            chain.doFilter(exchange);
        }

        @Override
        public String description() {
            return "Adds CORS headers to every response";
        }
    }
}
