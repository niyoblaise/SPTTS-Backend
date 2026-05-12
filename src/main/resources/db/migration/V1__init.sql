CREATE TYPE role_enum     AS ENUM ('PASSENGER','DRIVER','OPERATOR','ADMIN','RURA');
CREATE TYPE bus_status_enum AS ENUM ('ACTIVE','OFFLINE','OUT_OF_SERVICE');

CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       full_name     VARCHAR(120) NOT NULL,
                       phone         VARCHAR(20) UNIQUE,
                       email         VARCHAR(120) UNIQUE,
                       password      VARCHAR(255) NOT NULL,
                       role          role_enum NOT NULL,
                       created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE routes (
                        id            BIGSERIAL PRIMARY KEY,
                        name          VARCHAR(120) NOT NULL,
                        description   TEXT,
                        created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE route_stops (
                             id            BIGSERIAL PRIMARY KEY,
                             route_id      BIGINT REFERENCES routes(id) ON DELETE CASCADE,
                             name          VARCHAR(120) NOT NULL,
                             latitude      DOUBLE PRECISION NOT NULL,
                             longitude     DOUBLE PRECISION NOT NULL,
                             stop_order    INTEGER NOT NULL
);

CREATE TABLE buses (
                       id            BIGSERIAL PRIMARY KEY,
                       plate_number  VARCHAR(20) UNIQUE NOT NULL,
                       model         VARCHAR(60),
                       status        bus_status_enum DEFAULT 'ACTIVE',
                       driver_id     BIGINT REFERENCES users(id),
                       route_id      BIGINT REFERENCES routes(id),
                       created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bus_locations (
                               id            BIGSERIAL PRIMARY KEY,
                               bus_id        BIGINT REFERENCES buses(id) ON DELETE CASCADE,
                               latitude      DOUBLE PRECISION NOT NULL,
                               longitude     DOUBLE PRECISION NOT NULL,
                               accuracy      DOUBLE PRECISION,
                               speed         DOUBLE PRECISION,
                               recorded_at   TIMESTAMP NOT NULL,
                               created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reports (
                         id            BIGSERIAL PRIMARY KEY,
                         type          VARCHAR(40) NOT NULL,
                         payload       JSONB NOT NULL,
                         created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);