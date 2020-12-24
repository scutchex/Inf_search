import json
import string
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
from nltk.tokenize import word_tokenize

stop_words = set(stopwords.words('english'))
lemmatizer = WordNetLemmatizer()


def main():
    with open('q1.json', 'r') as f:
        data = json.loads(f.read())
    strField = ['title', 'overview']
    arrField = ['actors', 'directors', 'genres']
    goodDocument = []

    rank = [
        [4, 5, 3, 2, 1, 1, 2, 1, 1, 2, 5, 1, 1, 5, 1, 1, 2, 5, 1, 4],
        [5, 1, 1, 1, 1, 2, 1, 2, 4, 2, 1, 3, 1, 1, 1, 1, 2, 5, 3, 1],
        [2, 1, 1, 1, 1, 1, 3, 5, 1, 1, 2, 3, 3, 1, 1, 1, 1, 1, 4, 1],
        [4, 1, 1, 1, 5, 1, 1, 1, 4, 1, 1, 1, 5, 2, 2, 2, 1, 1, 2, 1],
        [2, 5, 1, 1, 1, 1, 4, 1, 3, 2, 1, 3, 1, 1, 1, 3, 2, 1, 1, 2]
    ]

    for i in range(5):
        with open('q' + str(i) + '.json', 'r') as f:
            data = json.loads(f.read())
        rIndex = 0
        for doc in data:
            nextItem = {}
            for field in arrField:
                value = doc[field]
                line = ' '.join(doc[field]) if value is not None ''
                nextItem[field] = prepare(line)

            for field in strField:
                line = doc[field]
                nextItem[field] = prepare(line)

            nextItem['query'] = prepare(doc['query'])
            nextItem['relevance'] = rank[i][rIndex]
            goodDocument.append(nextItem)
            rIndex += 1

        with open('q' + str(i) + 'g.json', 'w+') as f:
            f.write(json.dumps(goodDocument, indent=4, sort_keys=True))


def prepare(line):
    line = line.lower()
    line = line.translate(str.maketrans('', '', string.punctuation))
    return line.split(' ')

    ##word_tokens = word_tokenize(line)
    ##filtered_sentence = [w for w in word_tokens if not w in stop_words]
    ##filtered_sentence = [lemmatizer.lemmatize(w) for w in filtered_sentence]
    # return filtered_sentence


main()
