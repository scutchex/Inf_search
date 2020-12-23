import sys
import sqlite3
from os import getenv
from dotenv import load_dotenv
from urllib.error import HTTPError

from modules import newslist
from modules.parser import NewsParser

try:
    load_dotenv()
except:
    print('ERROR: Could not load .env file')
    sys.exit(2)

BASE_URL = getenv('BASE_URL')
NEWS_PAGE = getenv('NEWS_PAGE')

try:
    conn = sqlite3.connect('news.db')
    cursor = conn.cursor()
except Exception as e:
    print('ERROR: Could not connect to database.', e)
    sys.exit(2)

try:
    urls = newslist.get_urls()[:1000]
except HTTPError as http_error:
    print(f'ERROR fetching news links: {http_error}')
    sys.exit(2)

print('Fetching news content...')
for url in urls:
    print(f'Fetching from {url}...')
    try:
        parse = NewsParser(BASE_URL + url)
        content = parse.get_news_content()
    except HTTPError as http_error:
        print(f'ERROR fetching from {url}: {http_error}')
        print('Continuing...')
        continue

    try:
        cursor.execute('''
            INSERT OR REPLACE INTO news
            (url, title, category, date, description, content, journalist, tags)
            VALUES(
                :url,
                :title,
                :category,
                :date,
                :description,
                :content,
                :journalist,
                :tags
            )
        ''', content)
    except Exception as e:
        print('ERROR: Unable to insert content on', content['url'], e)
        conn.rollback()
        conn.close()
        sys.exit(2)

conn.commit()
print('All news fetched successfully')
conn.close()
