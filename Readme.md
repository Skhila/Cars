# Cars API

## Overview
The **Cars Project** is a Spring Boot application that manages cars and their engines. It provides a REST API for CRUD operations on `Car` and `Engine` entities, supports pagination, and includes validation and exception handling.

## Technologies Used
- Java 17
- Spring Boot
- Spring Data JPA
- Jakarta Validation
- Lombok
- PostgreSQL (or any JPA-compatible database)
- Gradle

## Database Schema
The project uses two tables:

### Car Table
| Column       | Type    | Constraints |
|-------------|--------|------------|
| id          | BIGINT | PRIMARY KEY, AUTO-GENERATED |
| model       | STRING | NOT NULL, MAX 20 CHARACTERS |
| year        | INT    | MIN 1940    |
| is_driveable | BOOLEAN | NOT NULL |
| engine_id   | BIGINT | FOREIGN KEY (Engine Table) |

### Engine Table
| Column       | Type    | Constraints |
|-------------|--------|------------|
| id          | BIGINT | PRIMARY KEY, AUTO-GENERATED |
| horse_power | INT    | POSITIVE    |
| capacity    | DOUBLE | POSITIVE    |

## REST API Endpoints

### Cars API (`/cars`)
| Method | Endpoint | Description |
|--------|---------|-------------|
| GET    | `/cars?page={page}&pageSize={size}` | Get paginated list of cars |
| GET    | `/cars/{id}` | Get car details by ID |
| POST   | `/cars` | Add a new car |
| PUT    | `/cars/{id}` | Update car details |
| DELETE | `/cars/{id}` | Delete a car |

### Engines API (`/engines`)
| Method | Endpoint | Description |
|--------|---------|-------------|
| GET    | `/engines?page={page}&pageSize={size}&capacity={capacity}` | Get paginated list of engines (filter by capacity optional) |
| POST   | `/engines` | Add a new engine |
| PUT    | `/engines/{id}` | Update engine details |
| DELETE | `/engines/{id}` | Delete an engine |

## Exception Handling
- `@ControllerAdvice` handles `MethodArgumentNotValidException` and `NotFoundException`.
- Returns structured error responses with an error code and message.

## Validation Constraints
- `CarRequest`:
    - `model`: Required, 1-20 characters.
    - `year`: Minimum 1940.
    - `engineId`: Must be a positive value.
- `EngineRequest`:
    - `horsePower`: Must be a positive integer.
    - `capacity`: Must be a positive double.

## Running the Application
1. Clone the repository.
2. Configure the database in `application.properties`.
3. Run the application using:
   ```sh
   ./gradlew bootRun
   ```
4. The API will be available at `http://localhost:8080`.
