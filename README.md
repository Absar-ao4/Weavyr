# Researcher Clustering Pipeline
## Advanced Multi-Model Clustering System for Academic Researcher Profiling

[![Python](https://img.shields.io/badge/Python-3.8%2B-blue.svg)](https://www.python.org/)
[![scikit-learn](https://img.shields.io/badge/scikit--learn-1.0%2B-orange.svg)](https://scikit-learn.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Architecture](#system-architecture)
3. [Data Pipeline](#data-pipeline)
4. [Clustering Models (9 Models)](#clustering-models)
5. [In-Depth: Feature Engineering Methods](#in-depth-feature-engineering-methods)
   - [TF-IDF (Term Frequency-Inverse Document Frequency)](#1-tf-idf-term-frequency-inverse-document-frequency)
   - [NMF (Non-negative Matrix Factorization)](#2-nmf-non-negative-matrix-factorization)
   - [LDA (Latent Dirichlet Allocation)](#3-lda-latent-dirichlet-allocation)
   - [Sentence-BERT (S-BERT)](#4-sentence-bert-s-bert)
6. [Evaluation Metrics](#evaluation-metrics)
7. [Output Formats](#output-formats)
8. [Usage Instructions](#usage-instructions)
9. [Performance Optimization](#performance-optimization)
10. [Technical Requirements](#technical-requirements)
11. [File Structure](#file-structure)
12. [Troubleshooting](#troubleshooting)

---

## Executive Summary

This codebase implements a **comprehensive researcher clustering pipeline** that processes approximately **370,000 academic researcher profiles** using **9 distinct clustering models**. The system analyzes research concepts and associated scores to group researchers into **2,500-3,000 meaningful clusters** based on their academic focus areas.

### Key Features:

- **9 Advanced Clustering Models:** Combines 5 feature engineering techniques (TF-IDF, Weighted TF-IDF, NMF, LDA, Sentence-BERT) with 4 clustering algorithms (MiniBatchKMeans, BisectingKMeans, Birch, HDBSCAN)
- **Scalable Architecture:** Optimized for large-scale datasets (370K+ researchers)
- **Multi-Modal Evaluation:** Three complementary metrics (Silhouette, Davies-Bouldin, Calinski-Harabasz)
- **Production-Ready Outputs:** Each model generates metrics CSV, visualization PNG, serialized pipeline (PKL), and cluster profiles (JSON)
- **Memory Efficient:** Smart subsampling and sparse matrix handling for large-scale processing

### What Makes This System Unique:

1. **Concept-Based Clustering:** Uses research concepts (not keywords) for semantic understanding
2. **Weighted Features:** Incorporates concept scores for importance weighting
3. **Noise Handling:** Advanced noise point remapping using KNN for HDBSCAN models
4. **Deep Learning Integration:** Leverages Sentence-BERT for state-of-the-art semantic embeddings
5. **Comprehensive Profiling:** Generates detailed cluster profiles with top concepts, citation statistics, and productivity metrics

---

## System Architecture

### High-Level Pipeline

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         INPUT DATA (CSV)                                 │
│  researchers_final_1.csv (~370K researchers)                            │
│  ├─ concepts: ["Machine Learning", "Neural Networks", ...]             │
│  ├─ concept_scores: [0.95, 0.87, ...]                                  │
│  ├─ works_count, cited_by_count, h_index, i10_index, etc.             │
│  └─ researcher_id, name, institution, etc.                             │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      DATA PREPROCESSING                                  │
│  ├─ Parse concept lists from strings                                   │
│  ├─ Handle missing values and duplicates                               │
│  ├─ Create weighted text from concepts + scores                        │
│  └─ Memory optimization and validation                                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    FEATURE ENGINEERING (5 METHODS)                       │
│  ┌───────────────┬──────────────┬─────────────┬──────────────────────┐  │
│  │   TF-IDF      │   Weighted   │     NMF     │         LDA          │  │
│  │  (Standard)   │    TF-IDF    │  (Topics)   │  (Probabilities)     │  │
│  │               │  (w/ scores) │             │                      │  │
│  └───────────────┴──────────────┴─────────────┴──────────────────────┘  │
│                            │                                             │
│                            ▼                                             │
│                  ┌────────────────────┐                                 │
│                  │   Sentence-BERT    │                                 │
│                  │  (Deep Embeddings) │                                 │
│                  └────────────────────┘                                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  CLUSTERING ALGORITHMS (4 TYPES)                         │
│  ┌──────────────┬─────────────────┬──────────┬────────────────────────┐ │
│  │ MiniBatch    │   Bisecting     │  Birch   │       HDBSCAN          │ │
│  │   K-Means    │    K-Means      │ (Memory  │  (Density-based +      │ │
│  │ (Scalable)   │ (Hierarchical)  │  Efficient)│   KNN remapping)     │ │
│  └──────────────┴─────────────────┴──────────┴────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        EVALUATION & ANALYSIS                             │
│  ├─ Silhouette Score (cluster cohesion)                                │
│  ├─ Davies-Bouldin Index (cluster separation)                          │
│  ├─ Calinski-Harabasz Score (variance ratio)                           │
│  ├─ Noise percentage analysis                                          │
│  └─ Memory and execution time tracking                                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      OUTPUTS (Per Model)                                 │
│  ├─ Metrics CSV: Performance metrics (1 row per model)                 │
│  ├─ PNG Graph: Visual comparison of 3 metrics                          │
│  ├─ PKL Pipeline: Serialized model + vectorizer for web deployment     │
│  └─ JSON Profiles: Cluster summaries with top concepts & statistics    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    FINAL COMPARISON & RANKING                            │
│  ├─ Comparison PNG: All 9 models side-by-side visualization            │
│  ├─ Ranking CSV: Models sorted by composite performance score          │
│  └─ Best model identification with detailed analysis                   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Processing Flow Summary

1. **Data Loading:** Load ~370K researcher profiles with concept lists
2. **Feature Engineering:** Transform concepts into numerical representations using 5 different methods
3. **Clustering:** Apply 4 clustering algorithms to each feature representation
4. **Evaluation:** Compute 3 metrics for each of 9 model combinations
5. **Output Generation:** Save 4 file types per model (36 files total)
6. **Comparison:** Generate comprehensive comparison visualizations

---

## Data Pipeline

### Input Data Structure

**File:** `researchers_final_1.csv`  
**Size:** ~370,000 rows  
**Key Columns:**

| Column | Type | Description | Example |
|--------|------|-------------|---------|
| `concepts` | String (JSON list) | Research concepts | `"['Machine Learning', 'Deep Learning']"` |
| `concept_scores` | String (JSON list) | Importance scores (0-1) | `"[0.95, 0.87, 0.76]"` |
| `works_count` | Integer | Number of publications | `127` |
| `cited_by_count` | Integer | Total citations received | `3,542` |
| `h_index` | Integer | H-index metric | `18` |
| `i10_index` | Integer | i10-index metric | `24` |
| `citation_velocity` | Float | Citations per year | `156.3` |
| `researcher_id` | String | Unique identifier | `"A2345678901"` |
| `display_name` | String | Researcher name | `"Dr. Jane Smith"` |
| `institution` | String | Affiliation | `"Stanford University"` |

### Data Preprocessing Steps

```python
# Step 1: Parse concept lists from string representation
df['_parsed_concepts'] = df['concepts'].apply(
    lambda x: ast.literal_eval(x) if isinstance(x, str) else []
)

# Step 2: Parse concept scores
df['_parsed_scores'] = df['concept_scores'].apply(
    lambda x: ast.literal_eval(x) if isinstance(x, str) else []
)

# Step 3: Create space-separated concept text
df['_concepts_text'] = df['_parsed_concepts'].apply(
    lambda lst: ' '.join(lst) if lst else ''
)

# Step 4: Create weighted text (concepts repeated by score)
# Example: concept "Machine Learning" with score 0.95 
# appears ~10 times in weighted text
df['_weighted_text'] = df.apply(
    lambda row: create_weighted_text(
        row['_parsed_concepts'], 
        row['_parsed_scores']
    ), 
    axis=1
)

# Step 5: Data validation and cleaning
df = df.drop_duplicates(subset=['researcher_id'])
df = df[df['_concepts_text'].str.len() > 0]  # Remove empty concepts
```

### Memory Management

The pipeline employs several memory optimization techniques:

1. **Sparse Matrix Storage:** Feature matrices stored in CSR (Compressed Sparse Row) format
2. **Garbage Collection:** Explicit memory cleanup after each model
3. **Incremental Processing:** Sentence-BERT uses batch processing (1024 samples/batch)
4. **Subsampling for Metrics:** Evaluation metrics computed on random sample (15K max) to reduce computation
5. **In-Place Operations:** Normalize operations performed in-place where possible

---

## Clustering Models

The system implements **9 distinct clustering models**, each combining a feature engineering technique with a clustering algorithm.

### Model Comparison Matrix

| Model # | Feature Engineering | Clustering Algorithm | Key Strength | Use Case |
|---------|---------------------|---------------------|--------------|----------|
| 1 | TF-IDF | MiniBatchKMeans | Fast, scalable | Baseline comparison |
| 2 | TF-IDF | BisectingKMeans | Hierarchical structure | Better initialization |
| 3 | TF-IDF | Birch | Memory efficient | Very large datasets |
| 4 | TF-IDF | HDBSCAN + KNN | Noise detection | Variable density clusters |
| 5 | Weighted TF-IDF | MiniBatchKMeans | Importance weighting | Score-aware clustering |
| 6 | NMF | MiniBatchKMeans | Topic discovery | Interpretable clusters |
| 7 | LDA | MiniBatchKMeans | Probabilistic topics | Statistical modeling |
| 8 | Sentence-BERT | MiniBatchKMeans | Semantic understanding | Best quality |
| 9 | Sentence-BERT | HDBSCAN + KNN | Semantic + density | Highest sophistication |

### Detailed Model Descriptions

#### MODEL 1: TF-IDF + MiniBatchKMeans

**Purpose:** Baseline model using standard text mining approach

**Feature Engineering:**
- TF-IDF vectorization on concept text
- Vocabulary size: 15,000 features
- SVD reduction: 15,000 → 100 dimensions
- L2 normalization for cosine similarity

**Clustering:**
- MiniBatchKMeans with k=3000
- Batch size: 4096 for memory efficiency
- Initialization: k-means++

**Mathematical Foundation:**
```
TF-IDF(t,d) = TF(t,d) × IDF(t)
where:
  TF(t,d) = frequency of term t in document d
  IDF(t) = log(N / df_t)
  N = total documents
  df_t = documents containing term t
```

**Advantages:**
- Fast execution (~2-3 minutes)
- Low memory footprint
- Proven baseline for text clustering

**Limitations:**
- Ignores concept importance scores
- Bag-of-words semantics (no word order)
- Cannot capture semantic similarity

---

#### MODEL 2: TF-IDF + BisectingKMeans

**Purpose:** Hierarchical divisive clustering with better initialization

**Feature Engineering:**
- Same TF-IDF pipeline as Model 1

**Clustering:**
- BisectingKMeans: top-down hierarchical approach
- Starts with all data in 1 cluster
- Recursively bisects largest cluster
- More stable than random initialization

**Algorithm Steps:**
```
1. Initialize: All points in single cluster C
2. While number of clusters < k:
   a. Select largest cluster C_max
   b. Apply 2-means to split C_max → C_a, C_b
   c. Choose split minimizing SSE (Sum of Squared Errors)
   d. Replace C_max with C_a and C_b
```

**Advantages:**
- Better cluster quality than random k-means
- Maintains hierarchical structure
- Less sensitive to initialization

**Limitations:**
- Slower than MiniBatchKMeans (~10-15 minutes)
- Higher memory usage

---

#### MODEL 3: TF-IDF + Birch

**Purpose:** Memory-efficient clustering for very large datasets

**Feature Engineering:**
- Same TF-IDF pipeline as Model 1

**Clustering:**
- BIRCH (Balanced Iterative Reducing and Clustering using Hierarchies)
- Builds CF-Tree (Clustering Feature Tree) incrementally
- Processes data in single pass
- Final global clustering on leaf nodes

**Clustering Feature (CF):**
```
CF = (N, LS, SS)
where:
  N = number of points
  LS = linear sum of points (Σx_i)
  SS = square sum of points (Σx_i²)

Additivity: CF₁ + CF₂ = (N₁+N₂, LS₁+LS₂, SS₁+SS₂)
```

**Advantages:**
- Very memory efficient
- Single-pass algorithm
- Incremental processing capability

**Limitations:**
- Sensitive to data order
- Parameter tuning (threshold, branching factor)

---

#### MODEL 4: TF-IDF + HDBSCAN (with KNN Noise Remapping)

**Purpose:** Density-based clustering with outlier detection

**Feature Engineering:**
- Same TF-IDF pipeline as Model 1

**Clustering:**
- HDBSCAN: Hierarchical Density-Based Spatial Clustering
- Automatically determines number of clusters
- Identifies noise points (outliers)
- **Novel:** Noise remapped to nearest cluster using KNN

**Advanced Feature: Noise Remapping**
```python
# HDBSCAN assigns label -1 to noise points
# Problem: Loses ~5-15% of data in noise
# Solution: KNN-based remapping

if noise_count > 0:
    # Train KNN on successfully clustered points
    knn = KNeighborsClassifier(n_neighbors=5)
    knn.fit(X[labels != -1], labels[labels != -1])
    
    # Predict cluster for noise points
    noise_predictions = knn.predict(X[labels == -1])
    labels[labels == -1] = noise_predictions
```

**Density Calculation:**
```
Core Distance (k-distance):
  core_k(p) = distance from p to k-th nearest neighbor

Mutual Reachability Distance:
  d_mreach(a,b) = max{core_k(a), core_k(b), d(a,b)}
```

**Advantages:**
- No need to specify k (clusters determined automatically)
- Handles arbitrary cluster shapes
- Robust to noise and outliers
- KNN remapping recovers lost data

**Limitations:**
- Computationally expensive (~20-30 minutes)
- Parameter tuning (min_cluster_size, min_samples)

---

#### MODEL 5: Weighted TF-IDF + MiniBatchKMeans

**Purpose:** Concept importance-aware clustering

**Feature Engineering (UNIQUE):**
```python
# Instead of simple "Machine Learning Machine Learning..."
# Use concept scores to weight repetitions

def create_weighted_text(concepts, scores):
    weighted_parts = []
    for concept, score in zip(concepts, scores):
        # Repeat concept based on score
        # score=1.0 → 10 repetitions
        # score=0.5 → 5 repetitions
        # score=0.1 → 1 repetition
        repetitions = max(1, int(score * 10))
        weighted_parts.extend([concept] * repetitions)
    return ' '.join(weighted_parts)

# Example:
# concepts = ["Machine Learning", "Deep Learning", "NLP"]
# scores = [0.95, 0.87, 0.42]
# Output: "Machine Learning Machine Learning Machine Learning Machine Learning Machine Learning Machine Learning Machine Learning Machine Learning Machine Learning Deep Learning Deep Learning Deep Learning Deep Learning Deep Learning Deep Learning Deep Learning Deep Learning NLP NLP NLP NLP"
```

**Mathematical Justification:**
- Higher scores → Higher term frequency → Higher TF-IDF weight
- Preserves relative importance in vector space
- Maintains interpretability

**Clustering:**
- Same MiniBatchKMeans as Model 1

**Advantages:**
- Incorporates domain knowledge (concept scores)
- Better separation of primary vs secondary research areas
- Simple yet effective weighting scheme

**Limitations:**
- Assumes linear relationship between score and importance
- May over-emphasize top concepts

---

#### MODEL 6: NMF + MiniBatchKMeans

**Purpose:** Topic-based clustering with interpretable components

**See detailed explanation in [NMF Section](#2-nmf-non-negative-matrix-factorization)**

---

#### MODEL 7: LDA + MiniBatchKMeans

**Purpose:** Probabilistic topic modeling approach

**See detailed explanation in [LDA Section](#3-lda-latent-dirichlet-allocation)**

---

#### MODEL 8: Sentence-BERT + MiniBatchKMeans

**Purpose:** State-of-the-art semantic embeddings

**See detailed explanation in [Sentence-BERT Section](#4-sentence-bert-s-bert)**

---

#### MODEL 9: Sentence-BERT + HDBSCAN (with KNN Remapping)

**Purpose:** Best of both worlds - semantic understanding + density-based clustering

**Feature Engineering:**
- Same Sentence-BERT embeddings as Model 8

**Clustering:**
- HDBSCAN for density-based clustering
- KNN remapping for noise handling

**Why This Combination:**
1. **Sentence-BERT:** Captures semantic similarity ("neural networks" ≈ "deep learning")
2. **HDBSCAN:** Finds natural cluster boundaries without specifying k
3. **KNN Remapping:** Ensures all researchers assigned to clusters

**Expected Performance:**
- Highest quality clusters (semantic coherence)
- Best handling of multi-disciplinary researchers
- Most computationally expensive (~45-60 minutes)

**Ideal For:**
- Final production deployment
- When interpretability matters
- When computation time is not critical

---

## In-Depth: Feature Engineering Methods

This section provides comprehensive explanations of the four key feature engineering methods specifically requested: **TF-IDF, NMF, LDA, and Sentence-BERT**.

---

### 1. TF-IDF (Term Frequency-Inverse Document Frequency)

#### Conceptual Overview

TF-IDF is a **numerical statistic** that reflects how important a word (concept) is to a document (researcher) in a collection (all researchers). It balances two competing factors:

1. **Term Frequency (TF):** How often does this concept appear for this researcher?
2. **Inverse Document Frequency (IDF):** How rare/common is this concept across all researchers?

**Intuition:** A concept that appears frequently for ONE researcher but rarely across ALL researchers is highly discriminative.

#### Mathematical Formulation

**Term Frequency (TF):**
```
TF(t, d) = f(t, d) / max{f(w, d) : w ∈ d}
```
Where:
- `t` = term (concept)
- `d` = document (researcher)
- `f(t, d)` = raw frequency of term t in document d
- Denominator = max frequency of any term in document d (normalization)

**Inverse Document Frequency (IDF):**
```
IDF(t, D) = log(N / df(t))
```
Where:
- `N` = total number of documents (researchers)
- `df(t)` = number of documents containing term t
- log = natural logarithm (smoothing)

**Smoothed IDF (used in this codebase):**
```
IDF(t, D) = log((1 + N) / (1 + df(t))) + 1
```
- Prevents zero division
- Adds 1 to avoid negative values

**Combined TF-IDF:**
```
TF-IDF(t, d) = TF(t, d) × IDF(t, D)
```

#### Example Calculation

**Dataset:**
- Researcher A: concepts = ["Machine Learning", "Deep Learning", "Computer Vision"]
- Researcher B: concepts = ["Machine Learning", "NLP", "Linguistics"]
- Researcher C: concepts = ["Quantum Computing", "Physics", "Algorithms"]
- Total: N = 3 researchers

**For Researcher A, concept "Machine Learning":**
```
TF("Machine Learning", A) = 1 / 3 = 0.333
  (appears 1 time, max frequency in doc is 3 terms)

IDF("Machine Learning") = log((1 + 3) / (1 + 2)) + 1
                        = log(4 / 3) + 1
                        = 0.288 + 1
                        = 1.288
  (appears in 2 out of 3 researchers)

TF-IDF("Machine Learning", A) = 0.333 × 1.288 = 0.429
```

**For Researcher A, concept "Computer Vision":**
```
TF("Computer Vision", A) = 1 / 3 = 0.333

IDF("Computer Vision") = log((1 + 3) / (1 + 1)) + 1
                       = log(4 / 2) + 1
                       = 0.693 + 1
                       = 1.693
  (appears in only 1 researcher - RARE!)

TF-IDF("Computer Vision", A) = 0.333 × 1.693 = 0.564
```

**Observation:** "Computer Vision" gets HIGHER weight than "Machine Learning" because it's rarer!

#### Implementation in Codebase

```python
from sklearn.feature_extraction.text import TfidfVectorizer

# Initialize TF-IDF vectorizer
tfidf = TfidfVectorizer(
    max_features=15000,    # Vocabulary size limit
    ngram_range=(1, 1),    # Unigrams only (single concepts)
    min_df=2,              # Ignore concepts in <2 researchers
    max_df=0.8,            # Ignore concepts in >80% of researchers
    sublinear_tf=True      # Use log(TF) instead of raw TF
)

# Fit and transform
tfidf_matrix = tfidf.fit_transform(df['_concepts_text'])
# Output: Sparse matrix (370000 × 15000)

# Dimensionality reduction with SVD
svd = TruncatedSVD(n_components=100, random_state=42)
reduced_tfidf = svd.fit_transform(tfidf_matrix)
# Output: Dense array (370000 × 100)

# L2 normalization (for cosine similarity)
reduced_tfidf = normalize(reduced_tfidf, norm='l2')
```

#### Why SVD Reduction?

**Problem:** 15,000 dimensions → computationally expensive, curse of dimensionality

**Solution:** Singular Value Decomposition (SVD)
```
X ≈ U × Σ × V^T

Truncated SVD keeps only top 100 components:
X₁₀₀ = U₁₀₀ × Σ₁₀₀ × V₁₀₀^T
```

**Benefits:**
- Removes noise (keep signal)
- Discovers latent semantic structure
- 150x dimension reduction (15K → 100)
- Preserves ~90% of variance

#### Advantages of TF-IDF

1. **Simple and Interpretable:** Easy to understand weights
2. **Proven Track Record:** 50+ years of use in information retrieval
3. **Fast Computation:** Vectorized operations
4. **Sparse Representation:** Memory efficient
5. **Language Agnostic:** Works with any text

#### Limitations of TF-IDF

1. **Bag-of-Words:** Ignores word order and context
2. **No Semantics:** "neural networks" and "deep learning" treated as unrelated
3. **Fixed Weights:** Cannot adapt to domain
4. **Requires Preprocessing:** Stemming, stop words, etc.
5. **Curse of Dimensionality:** High-dimensional sparse space

---

### 2. NMF (Non-negative Matrix Factorization)

#### Conceptual Overview

NMF is a **matrix decomposition technique** that discovers **latent topics** in text data. Unlike TF-IDF which treats each concept independently, NMF finds **groups of concepts that co-occur** and represents documents as **mixtures of topics**.

**Core Idea:** Your research profile is a combination of underlying research topics.

**Example:**
- Topic 1 (AI/ML): {machine learning: 0.8, neural networks: 0.7, deep learning: 0.9}
- Topic 2 (Computer Vision): {image processing: 0.9, computer vision: 0.8, object detection: 0.7}
- Researcher A = 70% Topic 1 + 30% Topic 2

#### Mathematical Formulation

**Objective:** Factorize non-negative matrix V into two non-negative matrices W and H

```
V ≈ W × H

Dimensions:
  V: (n_researchers × n_concepts)  = (370,000 × 15,000)
  W: (n_researchers × n_topics)    = (370,000 × 100)
  H: (n_topics × n_concepts)       = (100 × 15,000)

Constraint: All entries ≥ 0
```

**Matrix Interpretations:**
- **V**: Original TF-IDF matrix (researcher-concept matrix)
- **W**: Document-Topic matrix (researcher-topic weights)
- **H**: Topic-Concept matrix (topic definitions)

**Optimization Problem:**
```
minimize ||V - WH||²_F
subject to W ≥ 0, H ≥ 0

Where ||·||²_F is Frobenius norm:
||A||²_F = Σᵢ Σⱼ A²ᵢⱼ
```

#### Why Non-Negative?

**Non-negativity constraint** makes topics **interpretable as additive combinations**:
- Positive weights only (no "anti-topics")
- Easy to understand: "This researcher has X% of this topic"
- Parts-based representation

#### Multiplicative Update Rules

NMF uses iterative multiplicative updates (Lee & Seung, 1999):

**Update H (topic-concept matrix):**
```
H ← H ⊙ [(W^T V) / (W^T W H)]
```

**Update W (document-topic matrix):**
```
W ← W ⊙ [(V H^T) / (W H H^T)]
```

Where `⊙` is element-wise multiplication.

**Convergence:** Iterate until change in reconstruction error < tolerance

#### Example: Topic Discovery

**Input TF-IDF Matrix V (simplified):**
```
                ML    DL    CV    NLP   Quantum
Researcher A:  0.8   0.7   0.2   0.1   0.0
Researcher B:  0.3   0.2   0.8   0.7   0.1
Researcher C:  0.1   0.0   0.0   0.0   0.9
```

**After NMF with n_topics=2:**

**H (Topic-Concept Matrix):**
```
          ML    DL    CV    NLP   Quantum
Topic 1:  0.9   0.8   0.1   0.0   0.0      (AI/ML topic)
Topic 2:  0.0   0.0   0.9   0.8   0.1      (Vision/NLP topic)
```

**W (Researcher-Topic Matrix):**
```
              Topic 1   Topic 2
Researcher A:   0.85      0.15
Researcher B:   0.20      0.80
Researcher C:   0.05      0.05
```

**Interpretation:**
- Topic 1 = AI/Machine Learning research
- Topic 2 = Computer Vision/NLP research
- Researcher A: Primarily ML (85%)
- Researcher B: Primarily Vision/NLP (80%)
- Researcher C: Quantum computing (not well captured by these 2 topics)

#### Implementation in Codebase

```python
from sklearn.decomposition import NMF

# Start with TF-IDF matrix (non-negative by construction)
tfidf = TfidfVectorizer(max_features=15000, ...)
tfidf_matrix = tfidf.fit_transform(df['_concepts_text'])

# Apply NMF
nmf = NMF(
    n_components=100,         # Number of topics
    init='nndsvd',            # Non-negative SVD initialization
    max_iter=200,             # Maximum iterations
    random_state=42,
    alpha=0.0,                # L1 regularization (0 = no sparsity)
    l1_ratio=0.0              # L1 vs L2 ratio
)

# Transform: (370K × 15K) → (370K × 100)
nmf_features = nmf.fit_transform(tfidf_matrix)

# Normalize for clustering
nmf_features = normalize(nmf_features, norm='l2')
```

**Output:** Each researcher represented as 100-dimensional topic vector

#### Extracting Topic Interpretations

```python
# Get top concepts for each topic
feature_names = tfidf.get_feature_names_out()

for topic_idx, topic in enumerate(nmf.components_):
    # Get top 10 concepts for this topic
    top_indices = topic.argsort()[-10:][::-1]
    top_concepts = [feature_names[i] for i in top_indices]
    print(f"Topic {topic_idx}: {', '.join(top_concepts)}")
```

**Example Output:**
```
Topic 0: machine learning, neural networks, deep learning, AI, computer vision
Topic 1: quantum computing, quantum mechanics, physics, algorithms
Topic 2: bioinformatics, genomics, systems biology, computational biology
...
```

#### Advantages of NMF

1. **Interpretable Topics:** Clear semantic meaning
2. **Dimensionality Reduction:** 15K → 100 dimensions
3. **Additive Model:** Researchers = mixture of topics
4. **Non-negative:** Easy interpretation (no negative weights)
5. **Captures Co-occurrence:** Finds concept groups
6. **Better than PCA:** Non-negativity preserves interpretability

#### Limitations of NMF

1. **Requires Non-negative Input:** Cannot use raw counts (need TF-IDF or similar)
2. **Local Optima:** Solution depends on initialization
3. **Number of Topics:** Must specify k (topics) in advance
4. **Computationally Expensive:** Iterative optimization
5. **No Probabilistic Interpretation:** Unlike LDA (see next section)

#### NMF vs PCA

| Aspect | NMF | PCA |
|--------|-----|-----|
| Constraint | W, H ≥ 0 | No constraint |
| Interpretation | Additive parts | Orthogonal components |
| Components | Sparse, localized | Dense, global |
| Use Case | Topic modeling | General dim reduction |

---

### 3. LDA (Latent Dirichlet Allocation)

#### Conceptual Overview

LDA is a **probabilistic generative model** that assumes:
1. Each researcher has a **distribution over topics**
2. Each topic has a **distribution over concepts**
3. Each concept in a researcher's profile is drawn from a topic, which is drawn from the researcher's topic distribution

**Key Difference from NMF:** LDA is **fully probabilistic** with **Bayesian priors**.

#### Generative Story

**How LDA "imagines" a researcher profile was created:**

```
For each researcher r:
  1. Draw topic distribution: θᵣ ~ Dirichlet(α)
     Example: θᵣ = [0.6 (AI), 0.3 (Bioinfo), 0.1 (Physics)]
  
  For each concept position n in researcher r's profile:
    2a. Draw topic: zᵣₙ ~ Multinomial(θᵣ)
        Example: zᵣₙ = Topic 0 (AI) with probability 0.6
    
    2b. Draw concept: wᵣₙ ~ Multinomial(φ_zᵣₙ)
        Example: wᵣₙ = "machine learning" from Topic 0's concept distribution
```

**Intuition:** You (researcher) have research interests (topic distribution). When you write a paper, you pick a topic based on your interests, then pick concepts/keywords from that topic.

#### Mathematical Formulation

**Model Parameters:**
- **α (alpha):** Dirichlet prior on per-researcher topic distributions (hyperparameter)
  - Higher α → More uniform topic distribution (researcher works on many topics)
  - Lower α → Sparse topic distribution (researcher specializes)
  
- **β (beta):** Dirichlet prior on per-topic concept distributions (hyperparameter)
  - Higher β → Topics have broad concept coverage
  - Lower β → Topics are focused on few concepts

**Plate Notation:**
```
α → θᵣ → zᵣₙ → wᵣₙ ← φₖ ← β
     ↑         ↓
     └─────────┘
   (for each researcher r)
   (for each concept position n)
```

**Joint Probability:**
```
P(θ, φ, z, w | α, β) = 
  ∏ᵣ P(θᵣ | α) × 
  ∏ₖ P(φₖ | β) × 
  ∏ᵣ ∏ₙ P(zᵣₙ | θᵣ) × P(wᵣₙ | φ_zᵣₙ)
```

Where:
- θᵣ = topic distribution for researcher r
- φₖ = concept distribution for topic k
- zᵣₙ = topic assignment for concept n of researcher r
- wᵣₙ = observed concept n of researcher r

#### Dirichlet Distribution

**Why Dirichlet?** It's a distribution **over distributions**!

**Dirichlet(α) in 3D (3 topics):**
- Samples are probability vectors: [p₁, p₂, p₃] where p₁+p₂+p₃=1
- α = [0.1, 0.1, 0.1] → Sparse (most weight on 1 topic)
- α = [10, 10, 10] → Uniform (equal weight on all topics)

**Probability Density:**
```
Dir(θ | α) = [Γ(Σαᵢ) / ∏Γ(αᵢ)] × ∏θᵢ^(αᵢ-1)
```

#### Inference: Variational Bayes

**Goal:** Find posterior distribution P(θ, φ, z | w, α, β)

**Problem:** Exact inference is **intractable** (exponential complexity)

**Solution:** Variational inference - approximate posterior with simpler distribution

**Variational Distribution:**
```
q(θ, φ, z | γ, λ, ψ) ≈ P(θ, φ, z | w, α, β)

where q factorizes:
q(θ, φ, z) = ∏ᵣ q(θᵣ | γᵣ) × ∏ₖ q(φₖ | λₖ) × ∏ᵣ ∏ₙ q(zᵣₙ | ψᵣₙ)
```

**Variational Parameters:**
- γᵣ: Dirichlet parameter for researcher r's topic distribution
- λₖ: Dirichlet parameter for topic k's concept distribution
- ψᵣₙ: Multinomial parameter for concept n's topic assignment

**Update Equations (Coordinate Ascent VI):**
```
ψᵣₙᵢ ∝ exp{E[log θᵣᵢ] + E[log φᵢ,wᵣₙ]}

γᵣᵢ = αᵢ + Σₙ ψᵣₙᵢ

λᵢᵥ = βᵥ + Σᵣ Σₙ ψᵣₙᵢ × 𝕀(wᵣₙ = v)
```

Iterate until convergence (ELBO stops improving).

#### Implementation in Codebase

```python
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.decomposition import LatentDirichletAllocation

# Step 1: Create word count matrix (LDA needs counts, not TF-IDF)
count_vec = CountVectorizer(
    max_features=15000,
    min_df=2,
    max_df=0.8,
    ngram_range=(1, 1)
)
count_matrix = count_vec.fit_transform(df['_concepts_text'])

# Step 2: Fit LDA
lda = LatentDirichletAllocation(
    n_components=100,           # Number of topics
    max_iter=20,                # Max iterations (limited for speed)
    learning_method='online',   # Online variational Bayes
    learning_offset=50.0,       # Slow down early iterations
    random_state=42,
    batch_size=2048,            # Mini-batch size
    n_jobs=4                    # Parallel processing
)

# Step 3: Transform to topic distributions
lda_features = lda.fit_transform(count_matrix)
# Output: (370K × 100) - each row sums to 1 (probability distribution)

# Step 4: Normalize (for compatibility with clustering)
lda_features = normalize(lda_features, norm='l2')
```

#### Interpreting LDA Topics

```python
# Get feature names
feature_names = count_vec.get_feature_names_out()

# For each topic
for topic_idx, topic in enumerate(lda.components_):
    # Get top 10 concepts
    top_indices = topic.argsort()[-10:][::-1]
    top_concepts = [feature_names[i] for i in top_indices]
    topic_prob = topic[top_indices] / topic.sum()
    
    print(f"Topic {topic_idx}:")
    for concept, prob in zip(top_concepts, topic_prob):
        print(f"  {concept}: {prob:.3f}")
```

**Example Output:**
```
Topic 0:
  machine learning: 0.089
  neural networks: 0.076
  deep learning: 0.071
  artificial intelligence: 0.054
  computer vision: 0.043
  ...

Topic 1:
  quantum computing: 0.112
  quantum mechanics: 0.098
  quantum algorithms: 0.067
  ...
```

#### Advantages of LDA

1. **Fully Probabilistic:** Principled uncertainty quantification
2. **Interpretable:** Each researcher is a mixture of topics
3. **Bayesian Priors:** Incorporates domain knowledge (α, β)
4. **Handles Polysemy:** Words can belong to multiple topics
5. **Scalable:** Online learning for large datasets
6. **No Negative Weights:** Natural non-negativity

#### Limitations of LDA

1. **Bag-of-Words:** Ignores word order
2. **Computational Cost:** Slower than NMF
3. **Hyperparameter Tuning:** α and β require tuning
4. **Local Optima:** Variational inference finds local optimum
5. **Fixed Topics:** Number of topics must be specified

#### LDA vs NMF

| Aspect | LDA | NMF |
|--------|-----|-----|
| Model | Probabilistic | Algebraic |
| Output | Probability distributions | Non-negative weights |
| Interpretation | "P(topic\|researcher)" | "Amount of topic" |
| Inference | Variational Bayes | Multiplicative updates |
| Priors | Dirichlet (α, β) | None (optimization only) |
| Speed | Slower | Faster |
| Uncertainty | Yes (Bayesian) | No |

**When to use LDA:**
- Need probabilistic interpretation
- Want uncertainty estimates
- Statistical modeling framework

**When to use NMF:**
- Speed is critical
- Don't need probabilities
- Simpler implementation

---

### 4. Sentence-BERT (S-BERT)

#### Conceptual Overview

Sentence-BERT is a **modification of BERT** (Bidirectional Encoder Representations from Transformers) designed specifically for **semantic similarity tasks**. Unlike TF-IDF/NMF/LDA which treat concepts as independent words, S-BERT creates **dense vector embeddings** that capture **semantic meaning**.

**Revolutionary Idea:** "neural networks" and "deep learning" get SIMILAR embeddings even though they share no words!

#### Why Traditional BERT Doesn't Work for Clustering

**Standard BERT Problem:**
```python
# Traditional BERT for similarity
bert = BertModel.from_pretrained('bert-base')

# To compare two sentences A and B:
input = "[CLS] A [SEP] B [SEP]"
similarity = bert(input)  # Must process together!

# For 370K researchers, pairwise comparisons:
# O(n²) = 370,000² = 137 billion comparisons! ❌
```

**Sentence-BERT Solution:**
```python
# Encode each sentence independently
sbert = SentenceTransformer('all-MiniLM-L6-v2')

embed_A = sbert.encode("Machine learning and AI")
embed_B = sbert.encode("Deep learning systems")

# Compare with simple cosine similarity
similarity = cosine(embed_A, embed_B)  # ✓ Fast!

# For clustering: Encode once, cluster many times
# O(n) encoding + O(n log n) clustering ✓
```

#### Architecture: Siamese BERT Network

**Training Strategy:**
```
                 Input A              Input B
                    ↓                    ↓
              ┌──────────┐        ┌──────────┐
              │   BERT   │        │   BERT   │  (Shared weights!)
              └──────────┘        └──────────┘
                    ↓                    ↓
              ┌──────────┐        ┌──────────┐
              │ Pooling  │        │ Pooling  │
              └──────────┘        └──────────┘
                    ↓                    ↓
                Embedding A        Embedding B
                    └────────┬────────┘
                             ↓
                      Similarity Loss
```

**Key Components:**

1. **Shared BERT Encoder:** Same weights for both inputs
2. **Pooling Layer:** Convert token embeddings → sentence embedding
3. **Loss Functions:** Train for similarity

#### Pooling Strategies

**Input:** BERT outputs token embeddings for each word
```
"Machine learning and AI"
↓ BERT ↓
[h_CLS, h_Machine, h_learning, h_and, h_AI]
Each h is 384-dimensional
```

**Pooling Options:**

**1. CLS-token pooling (simple):**
```python
embedding = h_CLS  # Use special [CLS] token
```

**2. Mean pooling (used in codebase):**
```python
# Average all token embeddings
embedding = (h_Machine + h_learning + h_and + h_AI) / 4
```

**3. Max pooling:**
```python
# Element-wise maximum
embedding = max(h_Machine, h_learning, h_and, h_AI)
```

**Best Practice:** Mean pooling (used by 'all-MiniLM-L6-v2')

#### Training Objectives

**1. Classification (Softmax Loss):**
```
For sentence pair (A, B) with label y:

features = concat(embed_A, embed_B, |embed_A - embed_B|)
logits = softmax(W × features)
loss = CrossEntropy(logits, y)
```

**2. Regression (MSE Loss):**
```
For sentence pair (A, B) with similarity score s:

similarity = cosine(embed_A, embed_B)
loss = MSE(similarity, s)
```

**3. Triplet Loss (Ranking):**
```
For triplet (anchor, positive, negative):

loss = max(0, ||embed_anchor - embed_pos||² 
             - ||embed_anchor - embed_neg||² + margin)
```

The model is trained on **1 billion+ sentence pairs** from:
- SNLI (Stanford Natural Language Inference)
- Multi-NLI
- STS Benchmark (Semantic Textual Similarity)
- MS COCO captions
- Wikipedia sentence pairs

#### Model: all-MiniLM-L6-v2

**Specification:**
- **Base Model:** MiniLM (distilled from BERT)
- **Layers:** 6 transformer layers (vs 12 for BERT-base)
- **Hidden Size:** 384 dimensions
- **Parameters:** 22.7 million (vs 110M for BERT-base)
- **Speed:** 2x faster than BERT-base
- **Performance:** 95% of BERT-base quality

**Architecture:**
```
Input Text: "Machine learning neural networks deep learning"
              ↓
    Tokenization (WordPiece)
              ↓
    [CLS] machine learning neural networks deep learning [SEP]
              ↓
    Embedding Layer (token + position + segment)
              ↓
    Transformer Layers × 6
      ├─ Multi-Head Self-Attention (12 heads)
      ├─ Layer Normalization
      ├─ Feed-Forward Network (384 → 1536 → 384)
      └─ Layer Normalization
              ↓
    Mean Pooling
              ↓
    Output: 384-dimensional dense vector
```

#### Self-Attention Mechanism

**Core of Transformer:**
```
For each token i, compute attention over all tokens j:

Q = W_Q × h_i  (Query)
K = W_K × h_j  (Key)
V = W_V × h_j  (Value)

Attention(Q, K, V) = softmax(QK^T / √d_k) × V
```

**Multi-Head Attention:**
- 12 parallel attention mechanisms
- Each learns different relationships
- Example heads:
  - Head 1: Syntactic dependencies
  - Head 2: Semantic similarity
  - Head 3: Co-reference resolution
  - etc.

**Why Self-Attention > Bag-of-Words:**
- Captures context: "bank" in "river bank" vs "bank account"
- Long-range dependencies: Relates words far apart
- Bidirectional: Uses both left and right context

#### Implementation in Codebase

```python
from sentence_transformers import SentenceTransformer
import numpy as np

# Step 1: Load pre-trained model
print("Loading SentenceTransformer model...")
sbert_model = SentenceTransformer(
    'all-MiniLM-L6-v2',
    device='cpu'  # Use 'cuda' for GPU
)

# Step 2: Batch encoding for efficiency
batch_size = 1024  # Process 1024 researchers at a time
embeddings = []

print("Encoding concepts to embeddings...")
for i in range(0, len(df), batch_size):
    batch_texts = df['_concepts_text'].iloc[i:i+batch_size].tolist()
    
    # Encode batch
    batch_embeddings = sbert_model.encode(
        batch_texts,
        batch_size=batch_size,
        show_progress_bar=False,
        convert_to_numpy=True,
        normalize_embeddings=False  # We normalize later
    )
    
    embeddings.append(batch_embeddings)

# Step 3: Concatenate all batches
embeddings = np.vstack(embeddings)
# Output: (370,000 × 384) dense array

# Step 4: L2 normalization (for cosine similarity)
embeddings = normalize(embeddings, norm='l2')
```

**Processing Time:**
- CPU: ~30-45 minutes for 370K researchers
- GPU (Tesla V100): ~5-8 minutes

#### What Do Embeddings Capture?

**Example Similarity Comparisons:**

```python
# Semantically similar (different words)
embed1 = sbert.encode("machine learning algorithms")
embed2 = sbert.encode("neural network models")
cosine(embed1, embed2) = 0.87  # High similarity! ✓

# Semantically different (some shared words)
embed3 = sbert.encode("machine learning in biology")
embed4 = sbert.encode("machine learning in physics")
cosine(embed3, embed4) = 0.62  # Moderate similarity

# Completely different
embed5 = sbert.encode("quantum computing")
embed6 = sbert.encode("marine biology")
cosine(embed5, embed6) = 0.12  # Low similarity ✓
```

**Semantic Relationships Captured:**
- Synonymy: "AI" ≈ "Artificial Intelligence"
- Hypernymy: "Neural Networks" ⊂ "Deep Learning"
- Meronymy: "Convolutional Layers" ∈ "CNN"
- Domain similarity: "Computer Vision" ∼ "Image Processing"

#### Advantages of Sentence-BERT

1. **Semantic Understanding:** Captures meaning, not just words
2. **Context-Aware:** Same word, different meanings distinguished
3. **Transfer Learning:** Pre-trained on billion+ examples
4. **Dense Representations:** Every dimension is meaningful
5. **State-of-the-Art:** Best performance on STS benchmarks
6. **Multi-lingual:** Models available for 50+ languages
7. **Constant Dimensionality:** 384D regardless of text length

#### Limitations of Sentence-BERT

1. **Computational Cost:** Slow encoding (30-45 min on CPU)
2. **Memory Intensive:** Large model (22M parameters)
3. **Fixed Embeddings:** Cannot update for domain-specific knowledge
4. **Black Box:** Hard to interpret individual dimensions
5. **English-Centric:** Best performance on English text
6. **Context Length Limit:** Max 512 tokens

#### Comparison: S-BERT vs Traditional Methods

**Example: Two Researchers**

**Researcher A:** "machine learning, neural networks, deep learning"
**Researcher B:** "artificial intelligence, ML algorithms, neural nets"

**TF-IDF Similarity:**
```
Shared terms: None! (Different words)
Cosine similarity: 0.0 ❌
```

**S-BERT Similarity:**
```
Semantic understanding: Same concepts, different words
Cosine similarity: 0.92 ✓✓✓
```

**Why S-BERT is Best for This Dataset:**
1. Research concepts have **many synonyms** (ML, machine learning, artificial intelligence)
2. Researchers use **different terminology** for same ideas
3. **Domain-specific terms** require semantic understanding
4. **Quality matters** more than speed for production clustering

---

## Evaluation Metrics

The pipeline evaluates each clustering model using **three complementary metrics** that measure different aspects of cluster quality.

### 1. Silhouette Score

**What it measures:** How well each point fits in its assigned cluster vs neighboring clusters

**Formula:**
```
For each sample i:
  a(i) = average distance to points in same cluster
  b(i) = minimum average distance to points in other clusters
  
  s(i) = (b(i) - a(i)) / max(a(i), b(i))

Overall Score = Average of all s(i)
```

**Range:** [-1, 1]
- **s = 1:** Perfect clustering (far from neighbors)
- **s = 0:** On cluster boundary
- **s = -1:** Wrong cluster assignment

**Interpretation for This Dataset:**
- **Score > 0.5:** Excellent separation
- **Score 0.3-0.5:** Good quality
- **Score < 0.3:** Poor separation

**Why it's useful:**
- Combines cohesion (how tight are clusters) and separation (how far apart)
- No ground truth needed
- Easy to interpret

**Computational Note:**
- Full calculation: O(n²) - prohibitive for 370K samples
- This codebase: Random sample of 15K for speed

---

### 2. Davies-Bouldin Index

**What it measures:** Average similarity between each cluster and its most similar cluster

**Formula:**
```
For each cluster i:
  S_i = average distance from points to centroid
  M_ij = distance between centroids i and j
  
  R_ij = (S_i + S_j) / M_ij
  
DB = (1/k) × Σ max_j≠i (R_ij)
```

**Range:** [0, ∞)
- **Lower is better**
- DB = 0 → Perfect (infinite separation)
- High DB → Poor clustering (overlapping)

**Interpretation for This Dataset:**
- **DB < 1.0:** Excellent separation
- **DB 1.0-2.0:** Good quality
- **DB > 2.0:** Poor separation

**Why it's useful:**
- Directly measures cluster overlap
- Centroid-based (fast computation)
- Complements Silhouette (different perspective)

---

### 3. Calinski-Harabasz Score (Variance Ratio)

**What it measures:** Ratio of between-cluster variance to within-cluster variance

**Formula:**
```
Between-cluster dispersion:
  B_k = Σ n_i × ||μ_i - μ||²

Within-cluster dispersion:
  W_k = Σ Σ ||x - μ_i||²

CH = [B_k / (k-1)] / [W_k / (n-k)]
```

Where:
- k = number of clusters
- n = number of samples
- μ_i = centroid of cluster i
- μ = overall centroid

**Range:** [0, ∞)
- **Higher is better**
- High CH → Well-separated, compact clusters
- Low CH → Diffuse, overlapping clusters

**Interpretation for This Dataset:**
- **CH > 5000:** Excellent
- **CH 1000-5000:** Good
- **CH < 1000:** Poor

**Why it's useful:**
- Similar to F-statistic in ANOVA
- Fast computation (only needs centroids)
- Effective for convex clusters

---

### Metric Comparison Summary

| Metric | Range | Best Value | Measures | Speed |
|--------|-------|------------|----------|-------|
| Silhouette | [-1, 1] | 1 | Cohesion + Separation | O(n²) → sampled |
| Davies-Bouldin | [0, ∞) | 0 | Cluster similarity | O(nk) → fast |
| Calinski-Harabasz | [0, ∞) | ∞ | Variance ratio | O(nk) → fast |

**Why use all three?**
- Silhouette: Sample-level quality
- Davies-Bouldin: Cluster-level overlap
- Calinski-Harabasz: Global structure

Different metrics can disagree - no single "best" metric exists!

---

## Output Formats

Each of the 9 models generates **4 output files**, resulting in **36 files total** plus **2 comparison files**.

### Per-Model Outputs (×9)

#### 1. Metrics CSV: `{model_name}_metrics_{timestamp}.csv`

**Purpose:** Single-row CSV with all performance metrics

**Columns:**
- `model`: Model name (e.g., "TFIDF_MiniBatchKMeans")
- `status`: SUCCESS / WARNING / FAILED
- `n_clusters`: Number of clusters formed
- `noise_count`: Number of noise points (HDBSCAN only)
- `noise_percentage`: Percentage of noise
- `silhouette_score`: Cohesion/separation metric
- `davies_bouldin_index`: Cluster overlap metric
- `calinski_harabasz_score`: Variance ratio metric
- `memory_used_mb`: Memory delta (MB)
- `execution_time_sec`: Runtime (seconds)
- `memory_before_mb`: Memory before clustering
- `memory_after_mb`: Memory after clustering

**Example:**
```csv
model,status,n_clusters,noise_count,noise_percentage,silhouette_score,davies_bouldin_index,calinski_harabasz_score,memory_used_mb,execution_time_sec
TFIDF_MiniBatchKMeans,SUCCESS,3000,0,0.0,0.3421,1.2356,4523.12,256.4,142.3
```

**Use Case:** Import into Excel/Python for comparison

---

#### 2. PNG Graph: `{model_name}_metrics.png`

**Purpose:** Visual bar chart comparing 3 metrics

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│     Clustering Metrics — TFIDF_MiniBatchKMeans          │
├──────────────┬──────────────────┬──────────────────────┤
│ Silhouette   │ Davies-Bouldin  │ Calinski-Harabasz   │
│ Score        │ Index            │ Score                │
│ (↑ better)   │ (↓ better)       │ (↑ better)           │
│              │                  │                      │
│   ┌───┐      │     ┌───┐        │      ┌───┐          │
│   │   │      │     │   │        │      │   │          │
│   │0.34│     │     │1.24│        │      │4523│         │
│   └───┘      │     └───┘        │      └───┘          │
└──────────────┴──────────────────┴──────────────────────┘
  Clusters: 3000 | Noise: 0.00% | Time: 142.3s | Memory Δ: 256.4 MB
```

**Use Case:** Quick visual assessment, presentations

---

#### 3. PKL Pipeline: `{model_name}_{timestamp}.pkl`

**Purpose:** Serialized model for web deployment

**Contents:**
```python
{
    'model_name': 'TFIDF_MiniBatchKMeans',
    'timestamp': '20260219_200925',
    'vectorizer': TfidfVectorizer(...),  # or NMF, LDA, SentenceTransformer
    'reducer': TruncatedSVD(...),        # if applicable
    'clusterer': MiniBatchKMeans(...),   # trained model
    'feature_names': ['machine learning', ...],
    'n_clusters': 3000,
    'metrics': {...}
}
```

**Loading Example:**
```python
import pickle

# Load pipeline
with open('TFIDF_MiniBatchKMeans_20260219_200925.pkl', 'rb') as f:
    pipeline = pickle.load(f)

# Predict cluster for new researcher
new_concepts = "quantum computing quantum algorithms physics"
new_features = pipeline['vectorizer'].transform([new_concepts])
new_features = pipeline['reducer'].transform(new_features)
new_features = normalize(new_features, norm='l2')
cluster_id = pipeline['clusterer'].predict(new_features)[0]

print(f"Assigned to cluster: {cluster_id}")
```

**Use Case:** Production deployment, web apps, real-time prediction

---

#### 4. JSON Profiles: `{model_name}_{timestamp}.json`

**Purpose:** Cluster profiles for web visualization

**Structure:**
```json
{
  "model_metadata": {
    "model_name": "TFIDF_MiniBatchKMeans",
    "timestamp": "20260219_200925",
    "n_clusters": 3000,
    "total_researchers": 370000,
    "metrics": {
      "silhouette_score": 0.3421,
      "davies_bouldin_index": 1.2356,
      "calinski_harabasz_score": 4523.12
    }
  },
  "cluster_profiles": {
    "0": {
      "cluster_id": 0,
      "size": 145,
      "avg_works_count": 67.3,
      "avg_cited_by_count": 1247.8,
      "avg_h_index": 15.2,
      "avg_i10_index": 23.7,
      "avg_citation_velocity": 89.4,
      "top_concepts": [
        "machine learning",
        "neural networks",
        "deep learning",
        "computer vision",
        "artificial intelligence",
        "image processing",
        "convolutional networks",
        "object detection",
        "semantic segmentation",
        "transfer learning"
      ]
    },
    "1": {
      "cluster_id": 1,
      "size": 132,
      "avg_works_count": 54.2,
      ...
    }
  },
  "usage_guide": {
    "predict_new_researcher": "Load PKL file, transform concepts, predict cluster_id",
    "explore_clusters": "Browse cluster_profiles by cluster_id",
    "find_similar": "Compare new researcher's concepts to cluster top_concepts"
  }
}
```

**Use Case:** Web dashboard, API endpoints, cluster exploration

---

### Final Comparison Outputs (×2)

#### 5. Comparison PNG: `model_comparison_{timestamp}.png`

**Purpose:** Side-by-side comparison of all 9 models

**Layout:**
```
┌──────────────────────────────────────────────────────────┐
│        Model Comparison — All 9 Models                   │
├────────────────┬──────────────────┬─────────────────────┤
│ Silhouette     │ Davies-Bouldin  │ Calinski-Harabasz  │
│ Score          │ Index            │ Score               │
│ (↑ better)     │ (↓ better)       │ (↑ better)          │
│                │                  │                     │
│ SBERT_HDBSCAN  │ NMF_MiniBatch    │ SBERT_MiniBatch     │
│ ████████ 0.45  │ ████ 0.89        │ ████████ 6234       │
│ SBERT_MiniBatch│ TFIDF_Birch      │ LDA_MiniBatch       │
│ ███████ 0.42   │ █████ 1.02       │ ███████ 5821        │
│ LDA_MiniBatch  │ SBERT_HDBSCAN    │ NMF_MiniBatch       │
│ ██████ 0.38    │ ██████ 1.15      │ ██████ 5102         │
│ ...            │ ...              │ ...                 │
└────────────────┴──────────────────┴─────────────────────┘
```

**Use Case:** Model selection, performance overview

---

#### 6. Ranking CSV: `model_ranking_{timestamp}.csv`

**Purpose:** Models ranked by composite performance score

**Composite Score Formula:**
```
composite_score = 
  0.40 × silhouette_normalized +
  0.35 × (1 - davies_bouldin_normalized) +
  0.25 × calinski_harabasz_normalized
```

**Columns:**
- `model`: Model name
- `composite_score`: Weighted average (0-1, higher better)
- `silhouette_score`: Original score
- `davies_bouldin_index`: Original score
- `calinski_harabasz_score`: Original score
- `n_clusters`: Number of clusters
- `noise_percentage`: Noise %
- `execution_time_sec`: Runtime
- `rank`: 1-9 ranking

**Example:**
```csv
model,composite_score,silhouette_score,davies_bouldin_index,calinski_harabasz_score,rank
SBERT_HDBSCAN,0.8523,0.4521,0.8934,6234.12,1
SBERT_MiniBatchKMeans,0.8245,0.4234,0.9123,5821.34,2
LDA_MiniBatchKMeans,0.7834,0.3845,1.0234,5102.45,3
...
```

**Use Case:** Quick identification of best model

---

### Output Directory Structure

```
./clustered_outputs/
├── TFIDF_MiniBatchKMeans_metrics_20260219_200925.csv
├── TFIDF_MiniBatchKMeans_metrics.png
├── TFIDF_MiniBatchKMeans_20260219_200925.pkl
├── TFIDF_MiniBatchKMeans_20260219_200925.json
│
├── TFIDF_BisectingKMeans_metrics_20260219_200925.csv
├── TFIDF_BisectingKMeans_metrics.png
├── TFIDF_BisectingKMeans_20260219_200925.pkl
├── TFIDF_BisectingKMeans_20260219_200925.json
│
... (7 more models, 4 files each)
│
├── model_comparison_20260219_200925.png
├── model_ranking_20260219_200925.csv
└── error_log_20260219_200925.csv (if errors occurred)

Total: 38 files per run
```

---

## Usage Instructions

### Prerequisites

**System Requirements:**
- Python 3.8+
- 16GB+ RAM recommended (8GB minimum)
- 10GB free disk space
- CPU: 4+ cores recommended

**Install Dependencies:**
```bash
pip install numpy pandas scikit-learn matplotlib seaborn
pip install hdbscan sentence-transformers
pip install scipy psutil
```

**Verify Installation:**
```bash
python -c "import sklearn; import hdbscan; import sentence_transformers; print('OK')"
```

---

### Running the Pipeline

**1. Prepare Data:**
```bash
# Ensure CSV file exists
ls researchers_final_1.csv

# Check first few rows
head -n 5 researchers_final_1.csv
```

**2. Configure Parameters:**

Edit configuration cell in notebook:
```python
CSV_PATH         = "./researchers_final_1.csv"
OUTPUT_DIR       = "./clustered_outputs"
N_CLUSTERS       = 3000       # Adjust based on dataset size
REDUCED_DIM      = 100        # Topic/component count
MAX_FEATURES     = 15000      # Vocabulary size
SBERT_BATCH_SIZE = 1024       # Lower if RAM < 16GB
```

**3. Execute Pipeline:**

**Option A: Jupyter Notebook**
```bash
jupyter notebook codebase.ipynb
# Click "Run All" or run cells sequentially
```

**Option B: Python Script**
```bash
# Convert notebook to script
jupyter nbconvert --to python codebase.ipynb

# Run script
python codebase.py
```

**4. Monitor Progress:**

```
Config loaded | timestamp=20260219_200925 | output_dir=./clustered_outputs
Data loaded successfully | shape=(370245, 42) | memory=1523.4 MB

════════════════════════════════════════════════════════════════
MODEL 1 — TF-IDF + MiniBatchKMeans
════════════════════════════════════════════════════════════════
  TF-IDF vectorization... Done (15000 features)
  SVD reduction... Done (100 components)
  Clustering... Done (3000 clusters)
  ✓ Status: SUCCESS
    Clusters: 3000 | Noise: 0.00%
    Silhouette: 0.3421 | Davies-Bouldin: 1.2356 | Calinski-Harabasz: 4523.12
    Memory Δ: 256.4 MB | Time: 142.3s
  💾 Graph saved: TFIDF_MiniBatchKMeans_metrics.png
  💾 Metrics CSV: TFIDF_MiniBatchKMeans_metrics_20260219_200925.csv
  💾 Pipeline PKL: TFIDF_MiniBatchKMeans_20260219_200925.pkl
  💾 Profiles JSON: TFIDF_MiniBatchKMeans_20260219_200925.json

... (8 more models)

════════════════════════════════════════════════════════════════
FINAL COMPARISON
════════════════════════════════════════════════════════════════
  📊 Total Models: 9
  ✓ Successful: 9
  ✗ Failed: 0
  
  🏆 BEST MODEL: SBERT_HDBSCAN
     Composite Score: 0.8523
     Silhouette: 0.4521 | Davies-Bouldin: 0.8934
     Calinski-Harabasz: 6234.12
     Time: 2847.5s | Memory: 3421.2 MB

  💾 Comparison graph: model_comparison_20260219_200925.png
  💾 Ranking CSV: model_ranking_20260219_200925.csv

Pipeline complete! Total time: 4523.7s (75.4 minutes)
```

**5. Review Outputs:**
```bash
ls -lh ./clustered_outputs/

# View comparison graph
open model_comparison_20260219_200925.png

# Load best model
python
>>> import pickle
>>> with open('SBERT_HDBSCAN_20260219_200925.pkl', 'rb') as f:
...     pipeline = pickle.load(f)
>>> pipeline['metrics']
```

---

### Expected Runtime

| Model | Feature Engineering | Clustering | Total | Memory |
|-------|---------------------|------------|-------|--------|
| 1 | TF-IDF (2 min) | MiniBatchKMeans (1 min) | 3 min | 500 MB |
| 2 | TF-IDF (2 min) | BisectingKMeans (12 min) | 14 min | 800 MB |
| 3 | TF-IDF (2 min) | Birch (3 min) | 5 min | 600 MB |
| 4 | TF-IDF (2 min) | HDBSCAN+KNN (25 min) | 27 min | 1200 MB |
| 5 | Weighted TF-IDF (3 min) | MiniBatchKMeans (1 min) | 4 min | 600 MB |
| 6 | NMF (8 min) | MiniBatchKMeans (1 min) | 9 min | 700 MB |
| 7 | LDA (15 min) | MiniBatchKMeans (1 min) | 16 min | 800 MB |
| 8 | Sentence-BERT (40 min) | MiniBatchKMeans (1 min) | 41 min | 2000 MB |
| 9 | Sentence-BERT (40 min) | HDBSCAN+KNN (25 min) | 65 min | 3000 MB |

**Total Pipeline Runtime:** ~3 hours on CPU (16GB RAM, 4 cores)

**Optimization Tips:**
- Use GPU for Sentence-BERT (10x faster)
- Reduce `SBERT_BATCH_SIZE` to 512 if low memory
- Skip models 8-9 for faster iteration (use models 1-7)

---

## Performance Optimization

### Memory Management

**1. Sparse Matrix Usage:**
```python
# TF-IDF returns sparse CSR matrix by default
tfidf_matrix = tfidf.fit_transform(texts)
print(f"Sparsity: {1 - tfidf_matrix.nnz / (tfidf_matrix.shape[0] * tfidf_matrix.shape[1])}")
# Output: Sparsity: 0.9987 (99.87% zeros!)

# Memory saved: ~150x reduction
# Dense: 370K × 15K × 8 bytes = 44 GB
# Sparse: ~300 MB
```

**2. Garbage Collection:**
```python
import gc

# After each model
del tfidf_matrix, reduced_features, clusterer
gc.collect()
```

**3. Incremental Processing:**
```python
# Sentence-BERT batching
for i in range(0, len(df), batch_size):
    batch = df['_concepts_text'].iloc[i:i+batch_size]
    batch_embeddings = sbert_model.encode(batch)
    embeddings.append(batch_embeddings)
```

### Computational Speedups

**1. Parallel Processing:**
```python
# LDA with multiple cores
lda = LatentDirichletAllocation(n_jobs=4)

# Set environment variable
os.environ["LOKY_MAX_CPU_COUNT"] = "4"
```

**2. Reduced Dimensionality:**
```python
# SVD: 15K → 100 (150x reduction)
# Speeds up clustering by 100x
# Minimal quality loss
```

**3. Subsampling for Metrics:**
```python
# Full: O(370K²) = 137 billion comparisons
# Sampled: O(15K²) = 225 million comparisons
# 600x speedup!
```

**4. GPU Acceleration:**
```python
# Sentence-BERT on GPU
sbert_model = SentenceTransformer('all-MiniLM-L6-v2', device='cuda')
# 10x faster than CPU
```

---

## Technical Requirements

### Software Dependencies

**Core Libraries:**
```
numpy >= 1.20.0
pandas >= 1.3.0
scikit-learn >= 1.0.0
scipy >= 1.7.0
matplotlib >= 3.4.0
seaborn >= 0.11.0
```

**Clustering Algorithms:**
```
hdbscan >= 0.8.27
sentence-transformers >= 2.2.0
```

**Utilities:**
```
psutil >= 5.8.0
```

### Hardware Requirements

**Minimum:**
- CPU: 2 cores
- RAM: 8 GB
- Disk: 5 GB free
- Runtime: ~5 hours

**Recommended:**
- CPU: 4+ cores (or GPU for S-BERT)
- RAM: 16 GB+
- Disk: 10 GB+ free
- Runtime: ~2-3 hours

**Optimal:**
- CPU: 8+ cores
- GPU: NVIDIA GPU with 8GB+ VRAM (Tesla V100, RTX 3080, etc.)
- RAM: 32 GB+
- Disk: 20 GB+ SSD
- Runtime: ~45-60 minutes

---

## File Structure

```
researcher-clustering/
│
├── codebase.ipynb                 # Main Jupyter notebook
├── researchers_final_1.csv        # Input data (~370K researchers)
├── README.md                      # This file
│
├── clustered_outputs/             # Generated outputs (gitignored)
│   ├── *_metrics_*.csv           # Performance metrics (9 files)
│   ├── *_metrics.png             # Visualization graphs (9 files)
│   ├── *_*.pkl                   # Serialized pipelines (9 files)
│   ├── *_*.json                  # Cluster profiles (9 files)
│   ├── model_comparison_*.png    # Final comparison graph
│   ├── model_ranking_*.csv       # Model ranking table
│   └── error_log_*.csv           # Error log (if any)
│
└── docs/                          # Documentation (optional)
    ├── model_explanations.md
    ├── evaluation_metrics.md
    └── deployment_guide.md
```

---

## Troubleshooting

### Common Issues

**1. MemoryError during Sentence-BERT**
```
Error: Killed (process killed by OS)
```
**Solution:**
```python
# Reduce batch size
SBERT_BATCH_SIZE = 256  # Instead of 1024

# Or skip S-BERT models entirely
# Comment out MODEL 8 and MODEL 9 cells
```

**2. HDBSCAN Too Slow**
```
Model taking > 1 hour
```
**Solution:**
```python
# Reduce min_cluster_size
hdb = hdbscan.HDBSCAN(
    min_cluster_size=100,  # Instead of 50
    min_samples=20,        # Instead of 10
)
```

**3. ImportError: No module named 'sentence_transformers'**
```
ModuleNotFoundError: No module named 'sentence_transformers'
```
**Solution:**
```bash
pip install sentence-transformers
# Or for specific version:
pip install sentence-transformers==2.2.2
```

**4. ValueError: empty vocabulary**
```
ValueError: empty vocabulary; perhaps the documents only contain stop words
```
**Solution:**
```python
# Check data has content
print(df['_concepts_text'].iloc[0])

# Reduce min_df
tfidf = TfidfVectorizer(min_df=1)  # Instead of min_df=2
```

**5. Graph Not Displaying**
```
Matplotlib backend error
```
**Solution:**
```python
# For interactive display
matplotlib.use('TkAgg')  # Instead of 'Agg'
plt.show()  # Uncomment in plotting function
```

---

## License

This project is released under the MIT License. See LICENSE file for details.

---

## Acknowledgments

- OpenAlex for providing researcher data
- Hugging Face for pre-trained Sentence-BERT models
- scikit-learn team for machine learning infrastructure
- HDBSCAN authors for density-based clustering implementation

---
