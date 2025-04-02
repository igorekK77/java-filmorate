DROP TABLE rating CASCADE;
DROP TABLE film CASCADE;
DROP TABLE film_rating CASCADE;
DROP TABLE genre CASCADE;
DROP TABLE film_genre CASCADE;
DROP TABLE users CASCADE;
DROP TABLE user_friends CASCADE;
DROP TABLE likes CASCADE;


CREATE TABLE IF NOT EXISTS rating (
    rating_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS film (
    film_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(60) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration INTEGER
);

CREATE TABLE IF NOT EXISTS film_rating (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_id BIGINT REFERENCES film,
    rating_id INTEGER REFERENCES rating
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS film_genre (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_id BIGINT REFERENCES film,
    genre_id INTEGER REFERENCES genre
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    login VARCHAR(100) NOT NULL,
    name VARCHAR(60),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS user_friends (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users,
    friend_id BIGINT,
    status VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS likes (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_id BIGINT REFERENCES film,
    user_id BIGINT REFERENCES users
);