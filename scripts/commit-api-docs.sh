#!/bin/bash
set -e

# Parse command line arguments
VERSION=""
GIT_USER_NAME=""
GITHUB_TOKEN=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --version)
      VERSION="$2"
      shift 2
      ;;
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
      echo "Usage: $0 --version <version> --git-user-name <name> --github-token <token>"
      exit 1
      ;;
  esac
done

# Validate required parameter
if [ -z "$VERSION" ] || [ -z "$GIT_USER_NAME" ] || [ -z "$GITHUB_TOKEN" ]; then
  echo "Error: All parameters are required"
  echo "Usage: $0 --version <version> --git-user-name <name> --github-token <token>"
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

# Capture current branch
ORIGINAL_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Stash any uncommitted changes
git stash push -m "Temporary stash for docs commit" || true

# Add remote if it doesn't exist
if ! git remote | grep -q "^upstream$"; then
  git remote add upstream "${REMOTE_REPO}"
fi

# Fetch latest api-docs branch
git fetch upstream gh-pages

# Checkout api-docs branch (clean checkout)
git checkout -B gh-pages upstream/gh-pages

# Create docs directory if it doesn't exist
mkdir -p docs

# Copy documentation files
cp -r audioswitch/build/dokka/html "docs/${VERSION}"

# Create symlink for latest
rm -rf docs/latest
ln -s "docs/${VERSION}" docs/latest

# Add files to git
git add "docs/${VERSION}" docs/latest

# Commit
git commit -m "${VERSION} release docs [skip ci]"

# Push to origin
git push upstream gh-pages

# Return to original branch
git checkout "${ORIGINAL_BRANCH}"

# Apply stash if it exists
git stash pop || true
