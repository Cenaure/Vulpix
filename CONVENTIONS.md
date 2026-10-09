# Conventions

## Branching

- `main` is always stable and protected. Nobody pushes to it directly.
- `dev` is the integration branch. Features are merged into it.
- Work happens in short-lived branches created from `dev`:
    - `feature/<short-name>` for new functionality
    - `fix/<short-name>` for bug fixes
    - `docs/<short-name>` for documentation only
- `dev` is merged into `main` via a pull request when it is stable.

## Commits

We follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/):

```
<type>: <short description in imperative mood>
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.

## Workflow

```bash
# Start from the latest dev
git checkout dev
git pull origin dev

# Create a branch
git checkout -b feature/<name>

# Commit a change
# You'd better use IntelejI idea gui for that
git add .
git commit -m "feat: add feature"

# Push the branch (optional)
git push -u origin feature/<name>

# Before merging to dev bring the latest dev into the feature branch,
# resolve conflicts here and make sure the project builds
git fetch origin
git merge origin/dev
./gradlew spotlessApply

# Merge the feature branch into dev
git checkout dev
git pull --ff-only origin dev
git merge --no-ff feature/<name>
git push origin dev

# Clean up
git branch -d feature/<name>
git push origin --delete feature/<name>
```

## Code Style

- Format code using spotless:

```bash
  ./gradlew spotlessApply
```

- Use regions if you need to separate code into logical blocks.
```java
//region: # Entries

void CreateNewEntry() {
    //...
}

void UpdateEntry() {
    //...
}

//endregion: # Entries
```