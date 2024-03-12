## URL Shortener

This is a simple URL shortener application built using Java 17, Javalin and Maven.

### Running the Application

To run the application, you need to have Maven and Java 17 installed on your machine. Follow these steps:

1. Clone the repository to your local machine.
2. Navigate to the project directory.
3. Run the following Maven command to compile and package the application:

    `mvn clean compile assembly:single`

4. After successful compilation, run the following command to start the application:

    `java -jar target/UrlShortener-1.0-SNAPSHOT-jar-with-dependencies.jar`

5. The application will start and be accessible at `http://localhost:8080/`.

### Endpoints

The following endpoints are available:

- `GET /`: Displays a welcome message.
- `GET /{token}`: Redirects to the original URL associated with the provided token.
- `GET /original/{token}`: Retrieves the original URL associated with the provided token.
- `POST /shorten`: Shortens a long URL provided in the request body.
- `DELETE /short/{token}`: Deletes the short URL and associated long URL.

### Architecture

The application is structured using a layered architecture, separating concerns into different layers such as the controller, service, data access layer (DAL), and utilities. This is to promote modularity, maintainability, and scalability.

### Database

The application uses a SQLite database stored in a local file named `url_shortener_db.db` to store URL mappings. This SQLite database file is used for regular application operations. Additionally, for integration tests, a separate SQLite database file named `test_url_shortener_db.db` is utilized. Both database files are configured within the application's configuration files.

The SQL to for the Database can be seen below:

```SQL
-- Create the table to store original URLs
CREATE TABLE original_urls (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    expiration_date TIMESTAMP
);

-- Create the table to store the mappings between original URLs and tokens
CREATE TABLE tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    token TEXT NOT NULL UNIQUE,
    original_url_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(original_url_id) REFERENCES original_urls(id)
);

-- Create the table to store redirect analytics
CREATE TABLE redirect_analytics (
    original_url VARCHAR(255) NOT NULL,
    redirect_count INT DEFAULT 0,
    PRIMARY KEY (original_url)
);

CREATE INDEX "idx_tokens_original_url_id" ON "tokens" (
	"original_url_id"
)
```

### Dependencies

The project uses Javalin as the web framework and relies on Maven for dependency management. Other dependencies and plugins are specified in the `pom.xml` file.
