-- remove old table
DROP TABLE exercises;

-- lessons sync table
CREATE TABLE lessons_sync (
    id INTEGER PRIMARY KEY,
    title TEXT
);

-- lessons user table
CREATE TABLE lessons_user (
    id INTEGER PRIMARY KEY,
    disabled INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (id) REFERENCES lessons_sync (id)
);

-- lessons view
CREATE VIEW lessons AS
    SELECT
        lessons_sync.id,
        lessons_sync.title,
        lessons_user.disabled
    FROM
        lessons_sync LEFT OUTER JOIN lessons_user ON lessons_sync.id = lessons_user.id;

-- exercises sync table
CREATE TABLE exercises_sync (
    id INTEGER PRIMARY KEY,
    lesson_id INTEGER NOT NULL,
    scope TEXT NOT NULL,
    scope_letters TEXT NOT NULL UNIQUE,
    definition TEXT NOT NULL,
    notes TEXT,
    FOREIGN KEY (lesson_id) REFERENCES lesson(id)
);

-- exercises user table
CREATE TABLE exercises_user (
    id INTEGER PRIMARY KEY,
    easiness_factor REAL NOT NULL DEFAULT 2.5,
    practice_count INTEGER NOT NULL DEFAULT 0,
    practice_interval INTEGER NOT NULL DEFAULT 0,
    practice_time INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (id) REFERENCES exercises_sync (id)
);

-- exercises view
CREATE VIEW exercises AS
    SELECT
        exercises_sync.id,
        exercises_sync.lesson_id,
        lessons_sync.title AS lesson_title,
        exercises_sync.scope,
        exercises_sync.scope_letters,
        exercises_sync.definition,
        exercises_sync.notes,
        exercises_user.easiness_factor,
        exercises_user.practice_count,
        exercises_user.practice_interval,
        exercises_user.practice_time,
        lessons_user.disabled AS disabled
    FROM
        exercises_sync
        LEFT OUTER JOIN exercises_user ON exercises_sync.id = exercises_user.id
        LEFT JOIN lessons_sync ON exercises_sync.lesson_id = lessons_sync.id
        LEFT JOIN lessons_user ON exercises_sync.lesson_id = lessons_user.id;
