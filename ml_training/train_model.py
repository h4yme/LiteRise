#!/usr/bin/env python3
"""
LiteRise ML Model Training Script

Trains a neural network to predict student grade level placement
based on assessment response patterns.

Model Architecture:
- Input: 11 features (theta, accuracies, response times, etc.)
- Hidden layers: Dense layers with dropout
- Output: 5 classes (Grade 2, Low 3, Mid 3, High 3, Grade 4)

Author: LiteRise Development Team
"""

import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix
import matplotlib.pyplot as plt
import seaborn as sns
import os
import json

# Set random seeds for reproducibility
np.random.seed(42)
tf.random.set_seed(42)

class PlacementModelTrainer:
    def __init__(self):
        self.model = None
        self.scaler = StandardScaler()
        self.label_encoder = LabelEncoder()
        self.feature_names = None
        self.class_names = None

    def load_data(self, filepath='data/training_data.csv'):
        """Load and preprocess training data"""
        print("Loading training data...")
        df = pd.read_csv(filepath)

        # Define features
        self.feature_names = [
            'estimated_theta',
            'accuracy_rate',
            'oral_language_acc',
            'word_knowledge_acc',
            'reading_comp_acc',
            'language_struct_acc',
            'category_consistency',
            'avg_response_time',
            'response_time_std',
            'performance_trend',
            'questions_answered'
        ]

        # Extract features and labels
        X = df[self.feature_names].values
        y = df['placement'].values

        # Encode labels
        y_encoded = self.label_encoder.fit_transform(y)
        self.class_names = self.label_encoder.classes_

        print(f"✓ Loaded {len(df)} samples")
        print(f"  Features: {len(self.feature_names)}")
        print(f"  Classes: {self.class_names}")

        return X, y_encoded

    def split_data(self, X, y, test_size=0.2, val_size=0.1):
        """Split data into train, validation, and test sets"""
        print("Splitting data...")

        # First split: separate test set
        X_temp, X_test, y_temp, y_test = train_test_split(
            X, y, test_size=test_size, random_state=42, stratify=y
        )

        # Second split: separate validation set from training
        val_ratio = val_size / (1 - test_size)
        X_train, X_val, y_train, y_val = train_test_split(
            X_temp, y_temp, test_size=val_ratio, random_state=42, stratify=y_temp
        )

        print(f"✓ Train: {len(X_train)} | Val: {len(X_val)} | Test: {len(X_test)}")

        return X_train, X_val, X_test, y_train, y_val, y_test

    def normalize_features(self, X_train, X_val, X_test):
        """Normalize features using StandardScaler"""
        print("Normalizing features...")

        X_train_scaled = self.scaler.fit_transform(X_train)
        X_val_scaled = self.scaler.transform(X_val)
        X_test_scaled = self.scaler.transform(X_test)

        print("✓ Features normalized")

        return X_train_scaled, X_val_scaled, X_test_scaled

    def build_model(self, input_dim, num_classes):
        """Build neural network model"""
        print("Building model architecture...")

        model = tf.keras.Sequential([
            # Input layer
            tf.keras.layers.Input(shape=(input_dim,)),

            # Hidden layer 1
            tf.keras.layers.Dense(128, activation='relu'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.Dropout(0.3),

            # Hidden layer 2
            tf.keras.layers.Dense(64, activation='relu'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.Dropout(0.3),

            # Hidden layer 3
            tf.keras.layers.Dense(32, activation='relu'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.Dropout(0.2),

            # Output layer
            tf.keras.layers.Dense(num_classes, activation='softmax')
        ])

        # Compile model
        model.compile(
            optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
            loss='sparse_categorical_crossentropy',
            metrics=['accuracy']
        )

        print("✓ Model built successfully")
        print(model.summary())

        self.model = model
        return model

    def train(self, X_train, y_train, X_val, y_val, epochs=100, batch_size=32):
        """Train the model"""
        print("Training model...")

        # Callbacks
        callbacks = [
            tf.keras.callbacks.EarlyStopping(
                monitor='val_loss',
                patience=15,
                restore_best_weights=True
            ),
            tf.keras.callbacks.ReduceLROnPlateau(
                monitor='val_loss',
                factor=0.5,
                patience=5,
                min_lr=1e-6
            ),
            tf.keras.callbacks.ModelCheckpoint(
                'models/best_model.keras',
                monitor='val_accuracy',
                save_best_only=True
            )
        ]

        # Create models directory
        os.makedirs('models', exist_ok=True)

        # Train
        history = self.model.fit(
            X_train, y_train,
            validation_data=(X_val, y_val),
            epochs=epochs,
            batch_size=batch_size,
            callbacks=callbacks,
            verbose=1
        )

        print("✓ Training complete")

        return history

    def evaluate(self, X_test, y_test):
        """Evaluate model on test set"""
        print("Evaluating model...")

        # Predictions
        y_pred_probs = self.model.predict(X_test)
        y_pred = np.argmax(y_pred_probs, axis=1)

        # Calculate metrics
        test_loss, test_acc = self.model.evaluate(X_test, y_test, verbose=0)

        print(f"✓ Test Accuracy: {test_acc:.4f}")
        print(f"  Test Loss: {test_loss:.4f}")

        # Classification report
        print("\nClassification Report:")
        print(classification_report(
            y_test, y_pred,
            target_names=self.class_names
        ))

        # Confusion matrix
        cm = confusion_matrix(y_test, y_pred)

        return test_acc, test_loss, y_pred, cm

    def plot_training_history(self, history):
        """Plot training and validation metrics"""
        print("Generating training plots...")

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

        # Accuracy plot
        ax1.plot(history.history['accuracy'], label='Train Accuracy')
        ax1.plot(history.history['val_accuracy'], label='Val Accuracy')
        ax1.set_title('Model Accuracy')
        ax1.set_xlabel('Epoch')
        ax1.set_ylabel('Accuracy')
        ax1.legend()
        ax1.grid(True)

        # Loss plot
        ax2.plot(history.history['loss'], label='Train Loss')
        ax2.plot(history.history['val_loss'], label='Val Loss')
        ax2.set_title('Model Loss')
        ax2.set_xlabel('Epoch')
        ax2.set_ylabel('Loss')
        ax2.legend()
        ax2.grid(True)

        plt.tight_layout()
        plt.savefig('models/training_history.png', dpi=300)
        print("✓ Saved to models/training_history.png")

    def plot_confusion_matrix(self, cm):
        """Plot confusion matrix"""
        print("Generating confusion matrix plot...")

        plt.figure(figsize=(10, 8))
        sns.heatmap(
            cm,
            annot=True,
            fmt='d',
            cmap='Blues',
            xticklabels=self.class_names,
            yticklabels=self.class_names
        )
        plt.title('Confusion Matrix')
        plt.ylabel('True Label')
        plt.xlabel('Predicted Label')
        plt.tight_layout()
        plt.savefig('models/confusion_matrix.png', dpi=300)
        print("✓ Saved to models/confusion_matrix.png")

    def save_model(self):
        """Save model and metadata"""
        print("Saving model...")

        # Save full model
        self.model.save('models/placement_model.keras')

        # Save scaler parameters
        scaler_params = {
            'mean': self.scaler.mean_.tolist(),
            'scale': self.scaler.scale_.tolist(),
            'feature_names': self.feature_names
        }

        with open('models/scaler_params.json', 'w') as f:
            json.dump(scaler_params, f, indent=2)

        # Save label encoder
        label_mapping = {
            int(i): name for i, name in enumerate(self.class_names)
        }

        with open('models/label_mapping.json', 'w') as f:
            json.dump(label_mapping, f, indent=2)

        print("✓ Model saved to models/placement_model.keras")
        print("✓ Scaler params saved to models/scaler_params.json")
        print("✓ Label mapping saved to models/label_mapping.json")

def main():
    """Main training pipeline"""
    print("="*60)
    print("LiteRise ML Model Training Pipeline")
    print("="*60)
    print()

    # Initialize trainer
    trainer = PlacementModelTrainer()

    # Step 1: Load data
    X, y = trainer.load_data()

    # Step 2: Split data
    X_train, X_val, X_test, y_train, y_val, y_test = trainer.split_data(X, y)

    # Step 3: Normalize features
    X_train, X_val, X_test = trainer.normalize_features(X_train, X_val, X_test)

    # Step 4: Build model
    num_features = X_train.shape[1]
    num_classes = len(trainer.class_names)
    trainer.build_model(num_features, num_classes)

    # Step 5: Train model
    history = trainer.train(X_train, y_train, X_val, y_val, epochs=100, batch_size=32)

    # Step 6: Evaluate model
    test_acc, test_loss, y_pred, cm = trainer.evaluate(X_test, y_test)

    # Step 7: Plot results
    trainer.plot_training_history(history)
    trainer.plot_confusion_matrix(cm)

    # Step 8: Save model
    trainer.save_model()

    print()
    print("="*60)
    print("Training Complete!")
    print("="*60)
    print(f"Final Test Accuracy: {test_acc:.4f}")
    print(f"Model saved to: models/placement_model.keras")
    print()
    print("Next step: Run 'python export_to_tflite.py' to export to TensorFlow Lite")
    print("="*60)

if __name__ == '__main__':
    main()
