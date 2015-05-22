-- exercises table
CREATE TABLE exercises (
    _id INTEGER PRIMARY KEY,
    scope TEXT NOT NULL UNIQUE,
    definition TEXT NOT NULL,
    notes TEXT,
    practice_time INTEGER NOT NULL DEFAULT 0,
    rating INTEGER NOT NULL DEFAULT 0,
    sync INTEGER NOT NULL DEFAULT 0
);
