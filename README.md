# cooked-dinner Project 1 Instructions

## What To Do (EVERYONE PLEASE COMPLETE THESE STEPS)

1) Clone this repo onto your computer.

2) Use "checkout" or "switch" to create and move to a personal branch
   (named "your-first-name"):

   git checkout -b add-"Your Name"
   
   OR
   
   git switch -c add-"Your Name"
   

4) Use git status to confirm that you are on your personal branch and NOT main:

   git status

5) Open doc/team.txt, create a newline, and add ONLY your GitHub username.
   Example: rebeccairving

6) Stage your changes, but BEFORE you commit, confirm again that you are on your
   personal branch:

   git status

7) Run the following to ensure your personal branch is up to date with main
   (fix any merge issues on your personal branch — NOT during a main merge):



   git fetch origin

   
   git merge origin/main



   If there are merge issues:
   - Open doc/team.txt in VS Code.
   - It will prompt you to resolve the merge.
   - Accept ALL the incoming changes AND your changes.

9) Commit and push your changes.

   To set your branch as upstream use:
   
   git push --set-upstream origin "your personal branch name here"

10) Go to the GitHub browser page for this repo and create a PR.
   Add one or more other team members as a reviewer.

11) Check if any other team members have requested that you approve their PR.

12) Send a message to the Discord that you are done!!!
