ALTER DATABASE testdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE clients CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO CLIENTS (NAME,LAST_NAME)  VALUES
    ('Andres','Ruiz Peñuela'),
    ('Ramón','Poveda Paz'),
    ('Juana','Quesada Utrera');