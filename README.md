# Local ChatGPT Clone

A full-stack web application that provides ChatGPT-like functionality with local LLM integration.

## Features

- User authentication and authorization
- Context-aware chat windows
- Local LLM integration
- Docker containerization
- Spring Boot backend
- Modern React frontend

## Project Structure

```
.
├── backend/           # Spring Boot application
├── frontend/         # React application
├── docker/           # Docker configuration files
└── docker-compose.yml
```

## Prerequisites

- Java 17+
- Node.js 18+
- Docker
- Docker Compose
- Local LLM model (to be specified)

## Setup Instructions

1. Clone the repository
2. Configure the local LLM model path in `backend/src/main/resources/application.properties`
3. Build and run using Docker Compose:
   ```bash
   docker-compose up --build
   ```

## Development

### Backend Development
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm start
```

## License

MIT 