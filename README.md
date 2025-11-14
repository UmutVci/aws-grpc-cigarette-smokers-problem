# Distributed Cigarette Smokers Problem

A distributed implementation of the Cigarette Smokers synchronization
problem using a modern three-tier architecture:

-   **gRPC Simulation Backend (Spring Boot)**
-   **API Gateway (REST + SSE + gRPC Client)**
-   **React TypeScript Frontend**

## Overview

The system models the classical synchronization scenario as a
distributed, event-driven workflow.\
Real-time events flow through gRPC streaming → SSE → browser.

Branches: - `server`: gRPC simulation backend - `client`: API Gateway +
React UI

## Architecture

``` text
                 ┌──────────────────────────┐
                 │        Frontend          │
                 │  React + TS + SSE        │
                 └────────────▲─────────────┘
                              │
                        REST + SSE
                              │
                 ┌────────────┴─────────────┐
                 │       API Gateway        │
                 │  REST + gRPC Client      │
                 └────────────▲─────────────┘
                              │
                             gRPC
                              │
                 ┌────────────┴─────────────┐
                 │       gRPC Backend       │
                 │  Simulation & Streaming  │
                 └──────────────────────────┘
```

## Core Technologies

**Backend:** Java 21, Spring Boot, gRPC, Protobuf\
**Gateway:** Spring Web, SSE, gRPC Client\
**Frontend:** React, TypeScript, Vite, Tailwind

## Run Backend

``` bash
git checkout server
./mvnw spring-boot:run
```

gRPC: `localhost:9091`

## Run API Gateway

``` bash
git checkout client
./mvnw spring-boot:run
```

REST/SSE: `http://localhost:8080`

## Run Frontend

``` bash
npm install
npm run dev
```

UI: `http://localhost:8082`

## Author

Umut Avci
