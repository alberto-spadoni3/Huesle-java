# Huesle-java

## Description

This project aims to re-implement the backend part of [](Huesle) using Java and Vert.x.
One of the main goals of this work is to provide a server side implementation that includes distributed programming
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
    git clone https://dvcs.apice.unibo.it/pika-lab/courses/ds/projects/ds-project-agnoletti-spadoni-ay2021.git
    ```
2. Navigate into the project directory:
    ```bash
    cd ds-project-agnoletti-spadoni-ay2021
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