# MomTask AI

A simple Hindi/Hinglish AI reminder assistant built for my mother.

## Why I Built MomTask

MomTask converts natural Hindi/Hinglish reminders into structured tasks using local AI.

Example:

Kal subah doodh lana hai

becomes:

- Task: Bring milk
- Date: Tomorrow
- Time: Morning
- Reminder: 08:00 AM

## Features

- AI-powered Hindi/Hinglish task understanding
- Voice input using the microphone button
- Add, complete and delete tasks
- Browser reminder notifications
- Custom reminder time
- Local H2 database
- Ollama + Qwen3 1.7B
- One-click Windows launcher

## Tech Stack

Java 17, Spring Boot, Spring Data JPA, H2, Ollama, Qwen3 1.7B, HTML, CSS and JavaScript.

## Architecture

User -> MomTask UI -> Spring Boot -> Ollama/Qwen3
                         |
                         -> H2 Database

## Running

Install Java 17+ and Ollama, then run:

ollama pull qwen3:1.7b

./mvnw spring-boot:run

Open http://localhost:8080

## One-Click Launcher

Windows users can double-click MomTask.bat to start MomTask.

## Limitations

Browser notifications require the MomTask page to remain active. Voice input depends on browser Web Speech API support. Local AI performance depends on the computer hardware.

## Built For

This project was built for my mother as part of the Hacktoberfest 2026 DEV Launch Weekend Build for a Friend challenge.

## Author

Shitanshu Jha

Built with love for Mom.
