# LiteRise ML Training Pipeline

This directory contains the machine learning training pipeline for the LiteRise placement test.

## Overview

**Goal:** Train a neural network to predict student grade level placement based on assessment responses.

**Data Source:** Educational assessment data from multiple public sources
**Model Type:** TensorFlow neural network exported to TensorFlow Lite
**Deployment:** Android app (.tflite model)

## Pipeline Steps

1. **Data Collection** (`collect_data.py`)
   - Fetches educational assessment data from web sources
   - Combines multiple datasets
   - Saves to `data/training_data.csv`

2. **Training** (`train_model.py`)
   - Loads and preprocesses data
   - Trains neural network model
   - Evaluates performance
   - Saves model checkpoints

3. **Export** (`export_to_tflite.py`)
   - Converts TensorFlow model to TensorFlow Lite
   - Optimizes for mobile deployment
   - Saves as `placement_model.tflite`

4. **Deployment** (`../app/src/main/ml/`)
   - Copy .tflite file to Android assets
   - Use TensorFlow Lite interpreter in app

## Requirements

```bash
pip install -r requirements.txt
```

## Quick Start

```bash
# Step 1: Collect data from web
python collect_data.py

# Step 2: Train model
python train_model.py

# Step 3: Export to TensorFlow Lite
python export_to_tflite.py

# Step 4: Copy to Android
cp placement_model.tflite ../app/src/main/assets/
```

## Model Features

**Input Features:**
- Current theta estimate
- Accuracy rate
- Category-specific performance (4 categories)
- Response time statistics
- Question difficulty progression
- Number of questions answered

**Output:**
- Grade level prediction (Grade 2, Low 3, Mid 3, High 3, Grade 4)
- Confidence score (0-1)

## Data Sources

1. **NAEP Data Explorer** - National Assessment of Educational Progress
2. **TIMSS Dataset** - Trends in International Mathematics and Science Study
3. **Synthetic Data Generator** - For initial training when real data is limited
