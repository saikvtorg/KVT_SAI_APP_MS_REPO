#!/usr/bin/env bash
# Full journey: create exhibition -> module -> stall -> poster content -> user -> feedback -> quiz result
# Usage: ./scripts/full-journey.sh
set -euo pipefail

# Base URL: change to your deployed app if not running locally
BASE_URL="${BASE_URL:-http://localhost:8080}"

# helper: require jq
if ! command -v jq >/dev/null 2>&1; then
  echo "Error: jq is required to run this script. Install jq and retry." >&2
  exit 1
fi

echo "Using BASE_URL = $BASE_URL"
echo

# 1) Create Exhibition
echo "1) Creating exhibition..."
exh_resp=$(curl -sS -X POST "$BASE_URL/api/exhibitions" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "KVT Winter Expo 2025",
    "description": "Annual winter exhibition showcasing modules and stalls",
    "startDate": "2025-12-20",
    "endDate": "2025-12-25",
    "location": "Convention Center Hall A",
    "status": "CREATED"
  }')

echo "Exhibition response: $exh_resp"
EXH_ID=$(echo "$exh_resp" | jq -r '.exhibitionId // .id // empty')
if [[ -z "$EXH_ID" ]]; then
  echo "Failed to parse exhibition id from response." >&2
  exit 1
fi
echo "Exhibition created: $EXH_ID"
echo

# 2) Create Module under the exhibition
echo "2) Creating module for exhibition $EXH_ID..."
mod_resp=$(curl -sS -X POST "$BASE_URL/api/exhibitions/$EXH_ID/modules" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Interactive Tech Module",
    "description": "Module showcasing interactive tech demos",
    "assignedTeamId": "team-42"
  }')

echo "Module response: $mod_resp"
MODULE_ID=$(echo "$mod_resp" | jq -r '.moduleId // .id // empty')
if [[ -z "$MODULE_ID" ]]; then
  echo "Failed to parse module id from response." >&2
  exit 1
fi
echo "Module created: $MODULE_ID"
echo

# 3) Create Stall under the module
echo "3) Creating stall for module $MODULE_ID..."
stall_resp=$(curl -sS -X POST "$BASE_URL/api/modules/$MODULE_ID/stalls" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smart Devices Stall",
    "description": "Hands-on smart device demos",
    "stallNumber": "S-101",
    "layout": "2x2 booth"
  }')

echo "Stall response: $stall_resp"
STALL_ID=$(echo "$stall_resp" | jq -r '.stallId // .id // empty')
if [[ -z "$STALL_ID" ]]; then
  echo "Failed to parse stall id from response." >&2
  exit 1
fi
echo "Stall created: $STALL_ID"
echo

# 4) Create Poster Content for the stall
echo "4) Creating poster content for stall $STALL_ID..."
poster_resp=$(curl -sS -X POST "$BASE_URL/api/stalls/$STALL_ID/contents" \
  -H "Content-Type: application/json" \
  -d '{
    "languageCode": "en",
    "posterMediaUrl": "https://example.com/media/poster1.png",
    "contentText": "Welcome to the Smart Devices Stall! Try our AR demo."
  }')

echo "Poster content response: $poster_resp"
POSTER_ID=$(echo "$poster_resp" | jq -r '.contentId // .id // empty')
if [[ -z "$POSTER_ID" ]]; then
  echo "Failed to parse poster content id from response." >&2
  exit 1
fi
echo "Poster content created: $POSTER_ID"
echo

# 5) Create a User Profile
echo "5) Creating a user profile (visitor)..."
user_resp=$(curl -sS -X POST "$BASE_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Priya Kumar",
    "email": "priya.kumar@example.com",
    "phone": "+919900112233",
    "preferredLanguage": "en",
    "country": "India",
    "address": "123 MG Road, Bengaluru"
  }')

echo "User profile response: $user_resp"
USER_ID=$(echo "$user_resp" | jq -r '.userId // .id // empty')
if [[ -z "$USER_ID" ]]; then
  echo "Failed to parse user id from response." >&2
  exit 1
fi
echo "User profile created: $USER_ID"
echo

# 6) Create Feedback for the exhibition by the user
echo "6) Creating feedback for exhibition $EXH_ID by user $USER_ID..."
feedback_resp=$(curl -sS -X POST "$BASE_URL/api/feedbacks" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "'"${USER_ID}"'",
    "exhibitionId": "'"${EXH_ID}"'",
    "comments": "Great exhibition! Loved the interactive module.",
    "rating": 5
  }')

echo "Feedback response: $feedback_resp"
FEEDBACK_ID=$(echo "$feedback_resp" | jq -r '.feedbackId // .id // empty')
if [[ -z "$FEEDBACK_ID" ]]; then
  echo "Failed to parse feedback id from response." >&2
  exit 1
fi
echo "Feedback created: $FEEDBACK_ID"
echo

# 7) Create Quiz Result for the user against the module
echo "7) Creating quiz result for user $USER_ID on module $MODULE_ID..."
quiz_resp=$(curl -sS -X POST "$BASE_URL/api/quiz-results" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "'"${USER_ID}"'",
    "moduleId": "'"${MODULE_ID}"'",
    "result": 8,
    "totalMarks": 10,
    "points": 80
  }')

echo "Quiz result response: $quiz_resp"
QUIZ_ID=$(echo "$quiz_resp" | jq -r '.resultId // .id // empty')
if [[ -z "$QUIZ_ID" ]]; then
  echo "Failed to parse quiz result id from response." >&2
  exit 1
fi
echo "Quiz result created: $QUIZ_ID"
echo

echo "Full journey completed successfully."
echo "Exhibition: $EXH_ID"
echo "Module:     $MODULE_ID"
echo "Stall:      $STALL_ID"
echo "Poster:     $POSTER_ID"
echo "User:       $USER_ID"
echo "Feedback:   $FEEDBACK_ID"
echo "Quiz:       $QUIZ_ID"

