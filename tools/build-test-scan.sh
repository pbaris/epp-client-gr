#!/bin/bash
set -e

cd ..
clear

SONAR_PROJECT_KEY=epp-client-gr
SONAR_HOST_URL=http://localhost:9000
SONAR_TOKEN="${SONAR_TOKEN:?Set the SONAR_TOKEN environment variable before running this script}"
TAG_NAME="checkstyle"

function run_sonar_scan() {
  echo "Build, Test & Scan"
  ./gradlew clean build
  ./gradlew sonar \
    -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
    -Dsonar.host.url="$SONAR_HOST_URL" \
    -Dsonar.token="$SONAR_TOKEN"
}

function auto_tag_checkstyle_issues() {
  echo "Waiting for server to index issues..."
  sleep 15

  PAGE=1
  PAGE_SIZE=500

  while true; do
    echo "Fetching page $PAGE..."

    RAW_RESPONSE=$(curl -s -u "${SONAR_TOKEN}:" \
      "${SONAR_HOST_URL}/api/issues/search?projectKeys=${SONAR_PROJECT_KEY}&resolved=false&ps=${PAGE_SIZE}&p=${PAGE}")

    TOTAL_ISSUES=$(echo "$RAW_RESPONSE" | jq '.issues | length')

    if [ "$TOTAL_ISSUES" -eq 0 ]; then
      echo "No more issues returned by Sonar."
      break
    fi

    ISSUE_KEYS=$(echo "$RAW_RESPONSE" | jq -r '.issues[] | select(.externalRuleEngine == "checkstyle") | .key' | paste -sd "," -)

    if [ -n "$ISSUE_KEYS" ]; then
      echo "Tagging issues on page $PAGE"
      echo "ISSUE_KEYS=$ISSUE_KEYS"

      curl -s -u "${SONAR_TOKEN}:" -X POST \
        "${SONAR_HOST_URL}/api/issues/bulk_change?issues=${ISSUE_KEYS}&add_tags=${TAG_NAME}"
    else
      echo "No Checkstyle issues on page $PAGE"
    fi

    PAGE=$((PAGE + 1))
  done

  echo "All Checkstyle issues processed."
}

run_sonar_scan
auto_tag_checkstyle_issues
