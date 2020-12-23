import sys
import sqlite3

try:
    conn = sqlite3.connect('news.db')
    cursor = conn.cursor()
except Exception as e:
    print('ERROR: Could not connect to database.', e)
    sys.exit(2)

try:
    # `links` table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS links
        (id INTEGER PRIMARY KEY, url TEXT UNIQUE)
    ''')
    # `news` table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS news
        (
            id INTEGER PRIMARY KEY,
            url TEXT UNIQUE,
            title TEXT,
            category TEXT,
            date TEXT,
            description TEXT,
            content TEXT,
            journalist TEXT,
            tags TEXT
        )
    ''')
    # commit migrations
    conn.commit()
except Exception as e:
    print('ERROR:', e)
    conn.rollback()
finally:
    conn.close()
