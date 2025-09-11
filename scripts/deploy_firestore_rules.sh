#!/usr/bin/env bash
set -euo pipefail

# Deploy Firestore rules using Firebase CLI.
# Prereqs:
#  - npm i -g firebase-tools
#  - firebase login (or use CI token via --token)
#  - Firestore must be enabled in Firebase Console for project mealmanager-8471a

PROJECT_ID="mealmanager-8471a"

echo "Deploying Firestore rules to project: ${PROJECT_ID}"
firebase deploy --only firestore:rules --project "${PROJECT_ID}" "$@"

