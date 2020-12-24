import json

from sklearn.linear_model import LinearRegression
from math import log

def main():
    fields = ['title', 'overview', 'actors', 'directors', 'genres']
    search_results_size = 20
    query_count = 5
    x, y = [], []
    
    for i in range(query_count):
        with open('q' + str(i) + 'g.json', 'r') as f:
            data = json.loads(f.read())
        
        for doc in data:
            q = doc['query']
            xvec = []
            for field in fields:
                xvec.append(percent(q, doc[field]))
            x.append(xvec)
            y.append(int(doc['relevance']))

    """Linear Regression"""
    regeression = LinearRegression().fit(x, y)
    prediction = regeression.predict(x)

    for i in range(query_count):
        print('CHECK_' + str(i))
        start = search_results_size * i
        end = start + search_results_size
        checkNDCG(y[start:end], prediction[start:end])

def checkNDCG(y, prediction):
    idcgY = idcg(y)
    dcgY = dcg(y)

    dcgPrediction = dcg(sorted(prediction, reverse=True))

    print('OLD        DCG:  ', dcgY)
    print('PREDICTION DCG:  ', dcgPrediction )

    print('OLD        nDCG: ', dcgY / idcgY)
    print('PREDICTION nDCG: ', dcgPrediction / idcgY)

def percent(terms, zone):
    n = len(terms)
    i = 0
    for term in terms:
        if term in zone:
            i += 1       
    return i / n

def dcg(relevance):
    sum = 0
    i = 1
    filtered = [min(5, max(1, r)) for r in relevance]
    for r in filtered:
        sum += r / log(i + 1)
        i += 1
    return sum

def idcg(relevance):
    return dcg(sorted(relevance, reverse=True))


if __name__ == '__main__':
    main()