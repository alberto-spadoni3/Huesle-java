# Huesle-java

## Description

This project aims to re-implement the backend part of [Huesle](https://github.com/alberto-spadoni3/Huesle) switching
from JavaScript to Java and Vert.x.
One of the main goals of this artifact is to provide a server side implementation that includes distributed programming
techniques.

## Technologies Used

Frontend:

- JavaScript
- npm
- React

Backend:

- Java
- Vert.x
- MongoDB
- Gradle

Deployment:

- Docker

## Installation

This project uses Docker to build and deploy its microservices. Follow these steps to set it up on your local machine:

1. Clone the repository:
    ```bash
    git clone https://github.com/alberto-spadoni3/Huesle-java.git
    ```
2. Navigate into the project directory:
    ```bash
    cd Huesle-java
    ```
3. Build the Docker images:
    ```bash
    docker compose build
    ```
4. Start the services:
    ```bash
    docker compose up
    ```

## Usage

In order to use the application, you need to open your browser and navigate to `http://localhost:3000`.
This will open the Huesle web application where you can create an account, login and start playing with your friends!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
