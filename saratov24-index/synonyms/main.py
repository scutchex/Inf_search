import sys
from wiki_ru_wordnet import WikiWordnet

wikiwordnet = WikiWordnet()

def get_synonyms(word, n = 0, exclude = False):
    synonyms = []
    for synset in wikiwordnet.get_synsets(word):
        for synonym in synset.get_words():
            lemma = synonym.lemma()
            if lemma == word and exclude:
                continue
            synonyms.append(synonym.lemma())
    if n <= 0:
        return synonyms
    return synonyms[:n]

def main():
    try:
        word = sys.argv[1]
        number = int(sys.argv[2])
        syns = get_synonyms(word, number)
        for syn in syns:
            print(syn)
    except:
        pass

if __name__ == '__main__':
    main()
