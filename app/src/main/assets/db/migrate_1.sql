-- exercises table
CREATE TABLE exercises (
    _id INTEGER PRIMARY KEY,
    created_time INTEGER NOT NULL,
    updated_time INTEGER NOT NULL,
    scope TEXT NOT NULL UNIQUE,
    definition TEXT NOT NULL,
    notes TEXT,
    rating INTEGER NOT NULL DEFAULT 0,
    practice_time INTEGER NOT NULL DEFAULT 0,
    sync_time INTEGER
);
