# LiteRise ML Training Pipeline

This directory contains Python scripts to train machine learning models for the LiteRise placement test using real-world educational assessment data.

## Overview

The pipeline downloads public educational datasets, trains placement prediction models, and exports them to TensorFlow Lite format for Android integration.

## Setup

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

## Pipeline Steps

### 1. Data Collection (`collect_data.py`)
Downloads and combines multiple educational assessment datasets:
- NAEP (National Assessment of Educational Progress) data
- TIMSS (Trends in International Mathematics and Science Study)
- Synthetic data based on IRT parameters

### 2. Data Preprocessing (`preprocess_data.py`)
Cleans and prepares data:
- Feature engineering
- Normalization
- Train/validation/test split

### 3. Model Training (`train_model.py`)
Trains multiple models:
- Neural network for placement prediction
- Random forest for skill gap analysis
- Ensemble model combining both

### 4. Model Evaluation (`evaluate_model.py`)
Tests model performance:
- Accuracy, precision, recall
- Confusion matrix
- ROC curves

### 5. Export to TFLite (`export_tflite.py`)
Converts trained model to TensorFlow Lite:
- Quantization for mobile optimization
- Model compression
- Generates `.tflite` file for Android

## Usage

```bash
# Run complete pipeline
python train_pipeline.py

# Or run individual steps
python collect_data.py
python preprocess_data.py
python train_model.py
python export_tflite.py
```

## Output

- `models/placement_model.h5` - Trained Keras model
- `models/placement_model.tflite` - TensorFlow Lite model for Android
- `models/model_metrics.json` - Performance metrics
- `data/training_data.csv` - Collected training data

## Integration with Android

Copy the `.tflite` file to:
```
app/src/main/assets/placement_model.tflite
```

The Android app will use both:
1. Custom Java ML (PlacementMLPredictor.java) - immediate predictions
2. TFLite model (trained here) - enhanced accuracy

## Data Sources

- **NAEP Public Data**: https://nces.ed.gov/nationsreportcard/data/
- **TIMSS**: https://timssandpirls.bc.edu/timss-landing.html
- **Synthetic IRT Data**: Generated using research-validated parameters

## Model Architecture

```
Input Features (12):
- Current theta
- Accuracy per category (4)
- Response times
- Difficulty progression
- Category consistency
- Question count

Hidden Layers:
- Dense(64, relu)
- Dropout(0.3)
- Dense(32, relu)
- Dropout(0.2)

Output:
- Dense(5, softmax) - [Grade 2, Low 3, Mid 3, High 3, Grade 4]
```

## Performance Targets

- Accuracy: >85%
- Early prediction (after 15 questions): >80% accuracy
- Model size: <5MB
- Inference time: <50ms on mobile
