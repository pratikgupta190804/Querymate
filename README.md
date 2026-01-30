# QueryMate 🗃️💬

Chat with your database in your native language using AI! QueryMate allows you to query databases using natural language, making database interactions accessible to everyone.

## ✨ Features

- 🤖 **AI-Powered Queries**: Use natural language to query your databases
- 🔐 **Secure Authentication**: JWT-based authentication system
- 🌐 **Cloud Database Support**: Connect to databases hosted on AWS, Azure, Google Cloud, and more
- 🏠 **Local Database Support**: Works with local MySQL, PostgreSQL, MongoDB, and SQL Server
- 📊 **Automatic Schema Detection**: Automatically extracts and understands your database schema
- 🔒 **Encrypted Credentials**: All database credentials are securely encrypted
- 💬 **Chat Interface**: Interactive chat interface for database queries
- 📱 **Modern UI**: Clean, responsive React frontend

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven
- Node.js and npm
- MySQL (for the main application database)
- Ollama (for AI features)

### Backend Setup

1. Navigate to the backend directory:

```bash
cd QueryMate
```

2. Update `application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/query_mate
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Install dependencies and run:

```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:

```bash
cd querymate-frontend
```

2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## 🌩️ Cloud Database Support

QueryMate now supports connecting to cloud-hosted databases!

### Supported Cloud Providers

- **AWS RDS** (PostgreSQL, MySQL, SQL Server)
- **Azure SQL Database**
- **Azure Database for PostgreSQL/MySQL**
- **Google Cloud SQL**
- **MongoDB Atlas**
- **Azure Cosmos DB**
- **Amazon Aurora**

### Getting Started with Cloud Databases

1. Create a new project in QueryMate
2. Select "Cloud Database" as the connection type
3. Choose your cloud provider
4. Enter your connection string (JDBC URL)
5. Provide your database credentials

For detailed instructions and examples, see:

- [Cloud Database Guide](CLOUD_DATABASE_GUIDE.md)
- [Connection String Reference](CONNECTION_STRING_REFERENCE.md)

### Example: AWS RDS PostgreSQL

```
Connection String:
jdbc:postgresql://mydb.abc123.us-east-1.rds.amazonaws.com:5432/mydatabase?sslmode=require

Username: your_username
Password: your_password
```

## 🛠️ Technology Stack

### Backend

- **Spring Boot 3.5.3**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **Spring AI** (Ollama integration)
- **PostgreSQL, MySQL, SQL Server, MongoDB** JDBC Drivers
- **Java 17**

### Frontend

- **React**
- **Vite**
- **TailwindCSS**
- **Axios**
- **React Router**

## 📚 API Documentation

Once the backend is running, you can access the API documentation at:

```
http://localhost:8080/swagger-ui.html
```

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Encryption**: Bcrypt password hashing
- **Credential Encryption**: Database credentials encrypted before storage
- **SSL Support**: Full support for SSL/TLS database connections
- **CORS Configuration**: Configurable CORS for secure cross-origin requests

## 📖 Project Structure

```
QueryMate/
├── QueryMate/                  # Backend (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/querymate/QueryMate/
│   │   │   │       ├── auth/          # Authentication
│   │   │   │       ├── config/        # Configuration
│   │   │   │       ├── controller/    # REST Controllers
│   │   │   │       ├── dto/           # Data Transfer Objects
│   │   │   │       ├── entity/        # JPA Entities
│   │   │   │       ├── service/       # Business Logic
│   │   │   │       ├── repo/          # Repositories
│   │   │   │       └── utils/         # Utilities
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
│
└── querymate-frontend/         # Frontend (React)
    ├── src/
    │   ├── api/               # API Services
    │   ├── components/        # React Components
    │   ├── context/          # Context API
    │   ├── pages/            # Page Components
    │   └── routes/           # Routing
    └── package.json
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the MIT License.

## 🐛 Troubleshooting

### Connection Issues with Cloud Databases

1. **Verify Firewall Settings**: Ensure your IP is whitelisted in the cloud provider's firewall
2. **Check Connection String**: Verify the JDBC URL format is correct
3. **SSL/TLS**: Most cloud databases require SSL - make sure to include SSL parameters
4. **Credentials**: Double-check username and password

For more troubleshooting tips, see [Cloud Database Guide](CLOUD_DATABASE_GUIDE.md)

### Ollama Not Responding

1. Ensure Ollama is installed and running
2. Verify the base URL in application.properties
3. Check that the CodeLlama model is downloaded: `ollama pull codellama`

## 📞 Support

For issues and questions:

1. Check the documentation files in the project
2. Review error messages in the console
3. Ensure all dependencies are installed correctly

## 🎯 Future Enhancements

- [ ] Connection testing before saving
- [ ] Support for more AI models
- [ ] Query history and favorites
- [ ] Multi-database queries
- [ ] Export query results
- [ ] IAM authentication for AWS RDS
- [ ] Azure AD authentication for Azure SQL

---

Made with ❤️ by Pratik
