#!/bin/bash
set -e

# Parse command line arguments
VERSION=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --version)
      VERSION="$2"
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 --version <version>"
      exit 1
      ;;
  esac
done

# Validate required parameter
if [ -z "$VERSION" ]; then
  echo "Error: --version parameter is required"
  echo "Usage: $0 --version <version>"
  exit 1
fi

# Capture current branch
ORIGINAL_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Stash any uncommitted changes
git stash push -m "Temporary stash for docs commit" || true

# Fetch latest api-docs branch
git fetch origin gh-pages

# Checkout api-docs branch (clean checkout)
git checkout -B gh-pages origin/gh-pages

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
git push origin gh-pages

# Return to original branch
git checkout "${ORIGINAL_BRANCH}"

# Apply stash if it exists
git stash pop || true
