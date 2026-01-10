#!/bin/bash
set -e

# Parse command line arguments
GIT_USER_NAME=""
GITHUB_TOKEN=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --git-user-name)
      GIT_USER_NAME="$2"
      shift 2
      ;;
    --github-token)
      GITHUB_TOKEN="$2"
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 --git-user-name <name> --github-token <token>"
      exit 1
      ;;
  esac
done

# Validate required parameter
if [ -z "$GIT_USER_NAME" ] || [ -z "$GITHUB_TOKEN" ]; then
  echo "Error: All parameters are required"
  echo "Usage: $0 --git-user-name <name> --github-token <token>"
  exit 1
fi

# Set CIRCLE_PROJECT_USERNAME if not provided by environment
if [ -z "$CIRCLE_PROJECT_USERNAME" ]; then
  CIRCLE_PROJECT_USERNAME=$(git remote get-url origin | sed -n 's#.*github.com[:/]\([^/]*\)/.*#\1#p')
fi

# Set CIRCLE_PROJECT_REPONAME if not provided by environment
if [ -z "$CIRCLE_PROJECT_REPONAME" ]; then
  CIRCLE_PROJECT_REPONAME=$(git remote get-url origin | sed -n 's#.*github.com[:/].*/\(.*\)\.git#\1#p')
fi

# Create repo slug
REMOTE_REPO="https://${GIT_USER_NAME}:${GITHUB_TOKEN}@github.com/${CIRCLE_PROJECT_USERNAME}/${CIRCLE_PROJECT_REPONAME}.git"

# Add remote if it doesn't exist
if ! git remote | grep -q "^upstream$"; then
  git remote add upstream "${REMOTE_REPO}"
fi

# Fetch & check out the main branch
GIT_BRANCH=$(git remote show origin | grep HEAD | cut -d: -f2-)
git fetch "${REMOTE_NAME}"
git checkout "${GIT_BRANCH}"

# Read current version from gradle.properties
GRADLE_PROPERTIES="gradle.properties"
CURRENT_PATCH=$(grep "^versionPatch=" "$GRADLE_PROPERTIES" | cut -d'=' -f2)
NEXT_PATCH=$((CURRENT_PATCH + 1))

# Update gradle.properties
sed -i.bak "s/^versionPatch=.*/versionPatch=${NEXT_PATCH}/" "$GRADLE_PROPERTIES"
rm -f "${GRADLE_PROPERTIES}.bak"

# Commit and push
git add gradle.properties
git commit -m "Bump patch version [skip ci]"
git push upstream "${GIT_BRANCH}"

