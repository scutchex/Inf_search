from re import sub
from pymystem3 import Mystem
from json import loads, dumps
from nltk.corpus import stopwords
from os.path import dirname, abspath

d = dirname(dirname(abspath(__file__)))

rus_lemmatizer = Mystem()
rus_stopwords = stopwords.words('russian')

FILENAME = '5.json'
FILEPATH = f'{d}/resources/{FILENAME}'
OUTPATH = f'{d}/regression/{FILENAME}'
FIELDS = ['category', 'description', 'title', 'content', 'tags']


def preprocess_text(text):
    return [
        token for token in rus_lemmatizer.lemmatize(text.lower())
        if token not in rus_stopwords and token.strip() != ''
    ]


def make_ranking(query, results):
    ranks = [0 for _ in range(len(results))]
    for token in query:
        for it, result in enumerate(results):
            for field in FIELDS:
                if token in result[field]:
                    ranks[it] += 1
    return ranks


def normalize_ranks(ranks):
    mean_rank = round(sum(ranks) / len(ranks))
    max_rank = max(ranks)
    min_rank = min(ranks)
    normal_ranks = []
    for rank in ranks:
        if rank == min_rank:
            normal_ranks.append(1)
        elif rank == max_rank:
            normal_ranks.append(5)
        elif rank > min_rank and rank <= mean_rank - 0.5:
            normal_ranks.append(2)
        elif rank > mean_rank - 0.5 and rank <= mean_rank + 0.5:
            normal_ranks.append(3)
        elif rank > mean_rank + 0.5 and rank <= max_rank:
            normal_ranks.append(4)
        else:
            normal_ranks.append(3)
    return normal_ranks


def load(path):
    with open(path, 'r', encoding='utf-8') as f:
        data = loads(f.read())
    results = data['results']
    for result in results:
        for field in FIELDS:
            result[field] = preprocess_text(result[field])
    query = preprocess_text(sub(r'[^а-я0-9 ]', '', data['query'].lower()))
    return query, results


def dump(data, path):
    jsonData = dumps(data, indent=2, sort_keys=True, ensure_ascii=False)
    with open(path, 'w+', encoding='utf-8') as f:
        f.write(jsonData)


def main():
    query, results = load(FILEPATH)
    ranks = make_ranking(query, results)
    normal_ranks = normalize_ranks(ranks)

    data = []
    for it, result in enumerate(results):
        item = {}
        for field in FIELDS:
            item[field] = result[field]
        item['query'] = query
        item['relevance'] = normal_ranks[it]
        data.append(item)

    dump(data, OUTPATH)


if __name__ == '__main__':
    main()
