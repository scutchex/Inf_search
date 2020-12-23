import sys
import sqlite3
from os import getenv
from dotenv import load_dotenv
from datetime import datetime, timedelta

from modules.parser import LinkParser

try:
    load_dotenv()
except:
    print('ERROR: Could not load .env file')
    sys.exit(2)

BASE_URL = getenv('BASE_URL')
NEWS_PAGE = getenv('NEWS_PAGE')
FILTER_KEY = getenv('FILTER_KEY')
START_DATE = getenv('START_DATE')
END_DATE = getenv('END_DATE')
CONTAINER = getenv('CONTAINER')
FORMAT = getenv('FORMAT')


def get_date_range(start, end):
    start = datetime.strptime(start, '%Y-%m-%d')
    end = datetime.strptime(end, '%Y-%m-%d')

    delta = end - start
    days = []
    for day_num in range(delta.days + 1):
        day = start + timedelta(days=day_num)
        days.append(day.strftime('%d.%m.%Y'))
    return reversed(days)


def insert_links(cursor, links):
    dbLinks = [(link,) for link in links]
    cursor.executemany('INSERT OR REPLACE INTO links(url) VALUES(?)', dbLinks)


def get_urls(refetch=False):
    try:
        conn = sqlite3.connect('news.db')
        cursor = conn.cursor()
    except Exception as e:
        print('ERROR: Could not connect to database.', e)
        return set()

    if not refetch:
        cursor.execute('SELECT url FROM links')
        links = [row[0] for row in cursor]
        if len(links) > 0:
            return links
    else:
        cursor.execute('DELETE FROM links')

    filter_dates = get_date_range(START_DATE, END_DATE)
    urls = (f'{BASE_URL}{NEWS_PAGE}/?{FILTER_KEY}={date}' for date in filter_dates)

    links = set()
    print('Fetching links...')
    for url in urls:
        try:
            page_links = LinkParser(url).get_links(CONTAINER, FORMAT)
            insert_links(cursor, page_links)
            links.update(page_links)
        except:
            print(f'ERROR: error has occured while fetching links from {url}')
            print('Continuing...')

    try:
        conn.commit()
    except:
        print(f'ERROR: error has occured while writing to database')
        conn.rollback()

    conn.close()
    return links
