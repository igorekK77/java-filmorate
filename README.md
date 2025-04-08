# java-filmorate
Template repository for Filmorate project.
![Схема БД](https://github.com/igorekK77/java-filmorate/blob/main/%D0%94%D0%B8%D0%B0%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B0%20%D0%91%D0%94%20filmorate.jpg)


````

allFilms():
SELECT * FROM film;

getFilmById(Long id): 
SELECT * FROM film WHERE film_id = id;

getTopFilmsByLikes(int count): 
SELECT f.*, COUNT(user_id) AS count_likes
FROM film AS f 
JOIN like AS l ON f.film_id = l.film_id
GROUP BY f.film_id
ORDER BY count_likes DESC
LIMIT count;


allUser():
SELECT * FROM user;

getUserById(Long id):
SELECT * FROM user WHERE user_id = id; 

printUserFriends(Long id):
SELECT uf.friend_id, u.name, u.login
FROM user_friends AS uf 
JOIN user AS u ON uf.friend_id = u.user_id
WHERE uf.status = 'confirmed' AND uf.user_id = id;

printListCommonFriends(Long idFirstUser, Long idSecondUser):
SELECT uf.friend_id, u.name, u.login
FROM user_friends AS uf 
JOIN user AS u ON uf.friend_id = u.user_id
WHERE uf.status = 'confirmed' AND uf.user_id = idFirstUser
AND uf.friend_id IN (SELECT friend_id
FROM user_friends 
WHERE status = 'confirmed' AND user_id = idSecondUser);

````
