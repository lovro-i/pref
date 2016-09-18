# PREF
### Preference Data Mining Java Library

Preference data is present in many social and commercial contexts: voting, sports, search results, recommendations, etc. It comes in various forms: rankings, partial orders, top-k lists, pairwise comparisons, ratings...

This Java library provides means for managing, mining and modeling preference data. It is an ongoing work of Drexel University Database group (https://cs.drexel.edu/dbgroup/).

## 1. Installation

Clone this project...



## 2. Getting Started

Everything starts by creatings an `ItemSet` - the set of items (elements, alternatives) that we make preference over. If in our applications users state their preference over movies, this set represents 20 movies:
```java
ItemSet items = new ItemSet(20); // creates a set of 20 items, with IDs 0..19
```

You can get an Item from the set by its id:
```java
Item item = items.getItemById(2);
```

You can assign a tag to an item. It can be any object it represents, or its name, or whatever you want:
```java
item.setTag("Twelve Monkeys");
```

You can also use convenience methods for naming items (starting with `.tag...()`). This one will name items with letters A, B, C...:
```java
items.tagLetters();
```

Class `Ranking` represents a ranking (complete or incomplete) of items:
```java
Ranking ranking = new Ranking(items); // create an empty ranking
ranking.add(items.getItemById(3));
ranking.add(items.getItemByTag("E"));
ranking.add(items.get(0)); // same as .getItemById()
```

Or, you can get the reference ranking (complete ranking with items in order by their ids), or a random ranking directly from the `ItemSet`:
```java
Ranking referenceRanking = items.getReferenceRanking();
Ranking randomRanking = items.getRandomRanking();
```

`Ranking` implements `PreferenceSet` interface. However, there are other types of preference sets that implement `PreferenceSet` interface. If we want to state pairwise preferences, such as { A > B, A > D, C > D } (that cannot be represented by a ranking), we can use `MapPreferenceSet`:
```java
MapPreferenceSet pref = new MapPreferenceSet(items);
pref.add(items.get(0), items.get(1));
pref.add(items.getItemByTag("A"), items.getItemByTag("D"));
pref.addByTag("C", "D");
```

`Sample` is a (weighted or unweighted) set of `PreferenceSets`:
```java
Sample<PreferenceSet> sample = new Sample<PreferenceSet>(items);
sample.add(ranking);
sample.add(randomRanking);
```

Now the sample has two rankings in it.


## 3. Mallows Model

Mallows model is a istance-based ranking model, which is a kind of Gaussian distribution for rankings, at least by its ubiquity in the world of preferences. It has two parameters: central ranking (σ) and spread (ϕ).


You can create a Mallows model object:
```java
MallowsModel model = new MallowsModel(items.getRandomRanking(), 0.3d);
```

And easily sample 5000 rankings from it:
```java
Sample<Ranking> mallowsSample = MallowsUtils.sample(model, 5000);
```
