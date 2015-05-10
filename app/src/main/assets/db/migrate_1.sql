-- items table
CREATE TABLE items (
    _id INTEGER PRIMARY KEY,
    problem TEXT NOT NULL,
    solution TEXT NOT NULL,
    notes TEXT,
    rating INTEGER NOT NULL DEFAULT 3,
    tested INTEGER NOT NULL DEFAULT 0
);
