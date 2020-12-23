import re
from bs4 import BeautifulSoup
from urllib.request import urlopen


class ElementDecorator:
    def __init__(self, element=None):
        self.element = element

    def get_text(self):
        if self.element:
            stemmed = self.element.get_text().strip().lower()
            stemmed = re.sub(r'[^а-я0-9 ]', '', stemmed)
            return re.sub(r'\s+', ' ', stemmed)
        return ''

    def find(self, *args, **kwargs):
        if self.element:
            return ElementDecorator(self.element.find(*args, **kwargs))
        return ElementDecorator()

    def find_all(self, *args, **kwargs):
        if self.element:
            found = self.element.find_all(*args, **kwargs)
            return [ElementDecorator(element) for element in found]
        return []


class Parser:
    def __init__(self, url):
        self.url = url
        html_page = urlopen(url)
        self.soup = BeautifulSoup(html_page, features='html.parser')


class LinkParser(Parser):
    def get_links(self, div_class=None, format=r'^https?://'):
        if div_class is not None:
            data = self.soup.find_all('div', attrs={'class': div_class})
        else:
            data = [self.soup]

        links = set()
        for div in data:
            anchors = div.find_all('a', attrs={'href': re.compile(format)})
            for link in anchors:
                links.add(link.get('href'))

        return links


class NewsParser(Parser):
    def get_news_content(self):
        headerEl = self.soup.find('div', attrs={'class': 'article-header'})
        header = ElementDecorator(headerEl)
        title = header.find('h1', attrs={'class': 'newsarticlehead'})
        category = header.find('div')
        date = header.find('span', attrs={'class': 'post-block--date'})

        bodyEl = self.soup.find('div', attrs={'class': 'article-body'})
        body = ElementDecorator(bodyEl)
        descriptions = body.find_all('h4')
        description = ' '.join([h4.get_text() for h4 in descriptions])
        journalist = body.find(lambda t: t.name == 'p' and 'Автор' in t.text)

        tags = self.soup.find_all('a', attrs={'class': 'tag-item'})
        tags = ' '.join([ElementDecorator(tag).get_text() for tag in tags])

        return {
            'url': self.url,
            'title': title.get_text(),
            'category': category.get_text(),
            'date': date.get_text(),
            'description': description.strip(),
            'content': body.get_text(),
            'journalist': journalist.get_text(),
            'tags': tags.strip()
        }
