MERGE INTO mpa_ratings (id, name) KEY(id) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

MERGE INTO genres (id, name) KEY(id) VALUES
    (1, 'COMEDY'),
    (2, 'DRAMA'),
    (3, 'CARTOON'),
    (4, 'THRILLER'),
    (5, 'DOCUMENTARY'),
    (6, 'ACTION');