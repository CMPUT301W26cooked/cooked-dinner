# cooked-dinner (CMPUT 301 Team Project)

This repo is the **project repo** for our CMPUT 301 team (Org: `CMPUT301W26cooked`, Repo: `cooked-dinner`).
It contains **everything** 

## Current Phase
- **Project Part 2** — product backlog, UI mockups + storyboard sequences, CRC cards, and consistent GitHub usage.

## Where everything lives

### Project Board (Backlog + Task Tracking)
- Location: **Organization → Projects → "Project Board"**
- This is where we have:
  - converting user stories into issues
  - story points + risk
  - progress (In Progress → Review → Done)
- The rules for columns, story points, risk, and issue making in general are in the Wiki!!!

### Wiki (Documentation + Instructions)
- Location: **Repo → Wiki** it's an actual tab in github like Pull Requests and Projects
- The Wiki contains:
  - board rules + definitions
  - git workflow + PR rules
  - UI mockup & storyboard workflow (and where images go)
  - CRC card workflow

### Code
- Android Studio project code lives at the repo root 
- If we create additional documentation, it goes under `docs/`.

### UI Mockups + Storyboards (MUST be stored in GitHub)
- Folder: `docs/ui/`
  - `docs/ui/mockups/` (images/PDFs)
  - `docs/ui/storyboards/` (storyboard diagrams)
  - `docs/ui/decisions/` (decision notes like which library were using, colors, etc...)

### CRC Cards
- Folder: `docs/crc/`
- Store CRC cards as images (PNG) pls


## Quick links (inside GitHub)
- **Board:** Org → Projects → Project Board
- **Wiki:** Repo → Wiki
- **Issues:** Repo → Issues
- **Pull Requests:** Repo → Pull requests

---


## Team Workflow (Git + PR)

### 1) Clone the repo

```bash
git clone <repo-url>
cd cooked-dinner
```

### 2) Make a personal branch (never work directly on main)

Use your first name or a short handle:

```bash
git switch -c <your-branch-name>
# Example: git switch -c rebecca
```

### 3) Confirm you're not on main

```bash
git status
```

### 4) Keep your branch up-to-date with main

Do this before you open a PR:

```bash
git fetch origin
git merge origin/main
```

If you hit merge conflicts:
- resolve conflicts on **your branch**
- do **not** commit directly to `main`

### 5) Commit and push

```bash
git add -A
git commit -m "Short, clear message"
git push --set-upstream origin <your-branch-name>
```

### 6) Open a Pull Request (PR)

- Open PR from your branch → `main`
- Add at least 1 reviewer 
- Link the related Issue(s) in the PR description (example: `Closes #123`)

### 7) Review teammates’ PRs

- If someone requests your review pls try to respond quickly

---

## Notes

- Our submission must be **self-contained in GitHub** (no external submission links).
- UI diagrams/storyboards must be **embedded as images in the team wiki** (not external links).